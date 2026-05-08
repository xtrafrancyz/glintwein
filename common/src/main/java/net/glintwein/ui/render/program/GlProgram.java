package net.glintwein.ui.render.program;

import net.glintwein.platform.AutoQuadIndexBuffer;
import net.glintwein.platform.Platform;
import net.glintwein.util.ResourceLoaderUtil;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private int lastAutoIndexMaxSize = 0;

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
                    AutoQuadIndexBuffer indexBuffer = Platform.render().getQuadAutoIndexBuffer();
                    if (lastAutoIndexMaxSize <= 0 || !indexBuffer.hasCapacity(indexCount))
                        indexBuffer.bind(indexCount);
                    lastAutoIndexMaxSize = Math.max(lastAutoIndexMaxSize, indexCount);

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
        private final GlProgram program;
        private final int location;

        private float f0 = Float.NaN;
        private float f1 = Float.NaN;
        private float f2 = Float.NaN;
        private float f3 = Float.NaN;
        private int i0 = Integer.MIN_VALUE;
        private Matrix4f mat4 = null;

        public Uniform(GlProgram program, int location) {
            this.program = program;
            this.location = location;
        }

        public void setBool(boolean value) {
            setInt(value ? 1 : 0);
        }

        public void setFloat(float value) {
            if (this.f0 != value) {
                GL20.glUniform1f(location, value);
                this.f0 = value;
            }
        }

        public void setFloat4(float x, float y, float z, float w) {
            if (this.f0 != x || this.f1 != y || this.f2 != z || this.f3 != w) {
                GL20.glUniform4f(location, x, y, z, w);
                this.f0 = x;
                this.f1 = y;
                this.f2 = z;
                this.f3 = w;
            }
        }

        public void setColor4f(int color) {
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            float a = ((color >> 24) & 0xFF) / 255f;
            setFloat4(r, g, b, a);
        }

        public void setInt(int value) {
            if (this.i0 != value) {
                GL20.glUniform1i(location, value);
                this.i0 = value;
            }
        }

        public void setMat4(Matrix4f matrix) {
            if (this.mat4 == null || !mat4.equals(matrix)) {
                if (this.mat4 == null)
                    this.mat4 = new Matrix4f();
                this.mat4.set(matrix);

                try (MemoryStack stack = MemoryStack.stackPush()) {
                    FloatBuffer fb = this.mat4.get(stack.mallocFloat(16));
                    GL20.glUniformMatrix4fv(location, false, fb);
                }
            }
        }

        public void setTexture(int textureId) {
            int textureUnit = i0;
            if (textureUnit == Integer.MIN_VALUE) {
                textureUnit = program.samplerUnits.indexOf(this);
                if (textureUnit == -1) {
                    textureUnit = program.samplerUnits.size();
                    program.samplerUnits.add(this);
                }
                setInt(textureUnit);
            }
            Platform.render().stateActiveTexture(GL20.GL_TEXTURE0 + textureUnit);
            Platform.render().stateBindTexture(textureId);
        }
    }
}
