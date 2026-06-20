package net.glintwein.ui.render.program;

import net.glintwein.platform.AutoQuadIndexBuffer;
import net.glintwein.platform.Platform;
import net.glintwein.util.ResourceLoaderUtil;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GlProgram {
    public static final GlProgram MSDF = loadFromJar("msdf", GlintVertexFormat.MSDF);
    public static final GlProgram RECT = loadFromJar("rect", GlintVertexFormat.RECT);
    public static final GlProgram RECT_TEXTURED = loadFromJar("rect_textured", GlintVertexFormat.TEXTURED_RECT);

    private static final BufferBuilder BUFFER_BUILDER = new BufferBuilder(98304);

    private final GlintVertexFormat format;
    private final int programId;
    private final Map<String, Uniform> uniforms = new HashMap<>();
    private final List<Uniform> samplerUnits = new ArrayList<>();

    private final boolean vao;
    private int vaoId = -1;
    private int vboId = -1;
    private boolean vaoInitialized = false;

    public GlProgram(String vertexShader, String fragmentShader, GlintVertexFormat format) {
        this.format = format;

        int vertexShaderId = compileShader(vertexShader, GL20.GL_VERTEX_SHADER);
        int fragmentShaderId = compileShader(fragmentShader, GL20.GL_FRAGMENT_SHADER);

        this.programId = GL20.glCreateProgram();
        if (this.programId <= 0)
            throw new RuntimeException("Failed to create GL program (ID: " + this.programId + ")");

        GL20.glAttachShader(this.programId, vertexShaderId);
        GL20.glAttachShader(this.programId, fragmentShaderId);

        for (int i = 0; i < format.elements.length; i++) {
            GlintVertexFormatElement element = format.elements[i];
            GL20.glBindAttribLocation(this.programId, i, element.name);
        }

        GL20.glLinkProgram(this.programId);
        int linkStatus = GL20.glGetProgrami(this.programId, GL20.GL_LINK_STATUS);
        if (linkStatus == GL20.GL_FALSE) {
            String infoLog = GL20.glGetProgramInfoLog(this.programId);
            GL20.glDeleteProgram(this.programId);
            throw new RuntimeException("Failed to link GL program: " + infoLog);
        }

        vao = Platform.render().shouldUseVAO();
        if (vao) {
            vaoId = GL30.glGenVertexArrays();
            vboId = GL30.glGenBuffers();
        }
    }

    private void setupVertexAttribs(long pointer) {
        int stride = format.getVertexSize();
        int offset = 0;
        for (int i = 0; i < format.elements.length; i++) {
            GlintVertexFormatElement element = format.elements[i];
            int count = element.getByteSize() / element.type.size;
            GL20.glEnableVertexAttribArray(i);
            GL20.glVertexAttribPointer(i, count, element.type.glType, element.normalized, stride, pointer + offset);
            offset += element.getByteSize();
        }
    }

    public void bind() {
        GL20.glUseProgram(programId);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public GlintVertexConsumer begin() {
        BufferBuilder b = BUFFER_BUILDER;
        b.begin(GL20.GL_QUADS, format);
        return new GlintVertexConsumer(b);
    }

    public void draw() {
        BufferBuilder b = BUFFER_BUILDER;
        b.end();
        BufferBuilder.NextBuffer nextBuffer = b.popNextBuffer();
        nextBuffer.getBuffer().clear();
        BufferBuilder.DrawState state = nextBuffer.getState();
        if (state.vertexCount() > 0) {
            if (vao) {
                GL30.glBindVertexArray(vaoId);
                GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
                GL30.glBufferData(GL30.GL_ARRAY_BUFFER, nextBuffer.getBuffer(), GL30.GL_DYNAMIC_DRAW);
                if (!vaoInitialized) {
                    vaoInitialized = true;
                    setupVertexAttribs(0);
                }

                if (state.mode() == GL20.GL_QUADS) {
                    int indexCount = state.vertexCount() / 4 * 6;
                    AutoQuadIndexBuffer indexBuffer = Platform.render().getQuadAutoIndexBuffer(indexCount);
                    if (indexBuffer.getGlId() != -1)
                        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getGlId());
                    GL30.glDrawElements(GL11.GL_TRIANGLES, indexCount, indexBuffer.getGlType(), 0);
                } else {
                    GL20.glDrawArrays(state.mode(), 0, state.vertexCount());
                }

                GL30.glBindVertexArray(0);
            } else {
                long pointer = MemoryUtil.memAddress(nextBuffer.getBuffer());
                setupVertexAttribs(pointer);

                GL20.glDrawArrays(state.mode(), 0, state.vertexCount());

                for (int i = 0; i < format.elements.length; i++) {
                    GL20.glDisableVertexAttribArray(i);
                }
            }
        }

        Platform.render().stateActiveTexture(GL20.GL_TEXTURE0);
        unbind();
    }

    public Uniform getUniform(String name) {
        Uniform uniform = uniforms.get(name);
        if (uniform == null) {
            int location = GL20.glGetUniformLocation(programId, name);
            if (location == -1) {
                // Note: This often happens if the uniform is not used in the shader
                // code, as the GLSL compiler will optimize it away.
                System.err.println("Warning: Uniform '" + name + "' not found!");
            }
            uniform = new Uniform(this, location);
            uniforms.put(name, uniform);
        }
        return uniform;
    }

    private static int compileShader(String source, int shaderType) {
        int shaderId = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);
        int compileStatus = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);
        if (compileStatus == GL20.GL_FALSE) {
            String infoLog = GL20.glGetShaderInfoLog(shaderId);
            GL20.glDeleteShader(shaderId);
            throw new RuntimeException("Failed to compile shader: " + infoLog);
        }
        return shaderId;
    }

    private static GlProgram loadFromJar(String name, GlintVertexFormat format) {
        try (InputStream vshStream = ResourceLoaderUtil.getStream("/assets/shaders/" + name + ".vsh");
             InputStream fshStream = ResourceLoaderUtil.getStream("/assets/shaders/" + name + ".fsh")) {
            String vertexSource = ResourceLoaderUtil.toString(vshStream);
            String fragmentSource = ResourceLoaderUtil.toString(fshStream);
            return new GlProgram(vertexSource, fragmentSource, format);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader: " + name, e);
        }
    }

    public static class Uniform {
        private static final Vector4f TEMP_VECTOR4F = new Vector4f();

        private final GlProgram program;
        private final int location;

        private Object val = null;

        public Uniform(GlProgram program, int location) {
            this.program = program;
            this.location = location;
        }

        public void setBool(boolean value) {
            setInt(value ? 1 : 0);
        }

        public void setFloat(float value) {
            setCustomType(value, GL20::glUniform1f);
        }

        public void setColor4f(int color) {
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            float a = ((color >> 24) & 0xFF) / 255f;
            setFloat4(r, g, b, a);
        }

        public void setFloat4(float x, float y, float z, float w) {
            setCustomTypeCopy(
                TEMP_VECTOR4F.set(x, y, z, w),
                (loc, val) -> GL20.glUniform4f(loc, val.x, val.y, val.z, val.w),
                Vector4f::new,
                Vector4f::set
            );
        }

        public void setInt(int value) {
            setCustomType(value, GL20::glUniform1i);
        }

        public void setMat4(Matrix4f matrix) {
            setCustomTypeCopy(
                matrix,
                (loc, val) -> {
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        FloatBuffer fb = val.get(stack.mallocFloat(16));
                        GL20.glUniformMatrix4fv(loc, false, fb);
                    }
                },
                Matrix4f::new,
                Matrix4f::set
            );
        }

        public void setFloatArray(float[] values) {
            setCustomTypeClone(
                values,
                GL20::glUniform1fv,
                arr -> Arrays.copyOf(arr, arr.length)
            );
        }

        public void setTexture(int textureId) {
            int textureUnit;
            if (val == null) {
                textureUnit = program.samplerUnits.indexOf(this);
                if (textureUnit == -1) {
                    textureUnit = program.samplerUnits.size();
                    program.samplerUnits.add(this);
                }
                setInt(textureUnit);
            } else {
                textureUnit = (int) val;
            }
            Platform.render().stateActiveTexture(GL20.GL_TEXTURE0 + textureUnit);
            Platform.render().stateBindTexture(textureId);
        }

        /**
         * Helper method to set a uniform value that is an immutable type, where we can just compare the values directly.
         * <p>
         * Example:
         * <pre>{@code
         * uniform.setCustomType(1.0f, GL20::glUniform1f);
         * }</pre>
         */
        public <T> void setCustomType(T value, GlUniformSetter<T> setter) {
            if (!Objects.equals(this.val, value)) {
                setter.setUniform(location, value);
                this.val = value;
            }
        }

        /**
         * Helper method to set a uniform value that is a mutable type, where we want to copy the value before storing it.
         * <p>
         * Example:
         * <pre>{@code
         * Vector4f vec = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
         * uniform.setCustomTypeCopy(
         *     vec,
         *     (loc, val) -> GL20.glUniform4f(loc, val.x, val.y, val.z, val.w),
         *     Vector4f::new,
         *     Vector4f::set
         * );
         * }</pre>
         */
        public <T> void setCustomTypeCopy(T value, GlUniformSetter<T> setter, Supplier<T> constructor, BiConsumer<T, T> copy) {
            T val = (this.val != null) ? (T) this.val : null;
            if (val == null || !val.equals(value)) {
                if (val == null)
                    val = constructor.get();
                copy.accept(val, value);
                setter.setUniform(location, val);
                this.val = val;
            }
        }

        /**
         * Helper method to set a uniform value that is an array or other mutable type, where we want to clone the value before storing it.
         * <p>
         * Example:
         * <pre>{@code
         * float[] values = new float[]{1.0f, 2.0f, 3.0f};
         * uniform.setCustomTypeClone(
         *     values,
         *     GL20::glUniform1fv,
         *     arr -> Arrays.copyOf(arr, arr.length)
         * );
         * }</pre>
         */
        public <T> void setCustomTypeClone(T value, GlUniformSetter<T> setter, Function<T, T> clone) {
            T val = (this.val != null) ? (T) this.val : null;
            if (val == null || !val.equals(value)) {
                val = clone.apply(value);
                setter.setUniform(location, val);
                this.val = val;
            }
        }

        @FunctionalInterface
        public interface GlUniformSetter<T> {
            void setUniform(int location, T value);
        }
    }
}
