package net.glintwein.ui.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.datafixers.util.Pair;
import net.glintwein.util.ResourceLoader;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GlProgram {
    private static final Map<VertexFormatElement, AttribProperties> ATTRIBUTES = new HashMap<>();

    static {
        ATTRIBUTES.put(DefaultVertexFormat.ELEMENT_POSITION, new AttribProperties("Position", false));
        ATTRIBUTES.put(DefaultVertexFormat.ELEMENT_COLOR, new AttribProperties("Color", true));
        ATTRIBUTES.put(DefaultVertexFormat.ELEMENT_UV0, new AttribProperties("UV0", false));
        ATTRIBUTES.put(GlintVertexFormat.ELEMENT_POSITION2, new AttribProperties("Position", false));
        ATTRIBUTES.put(GlintVertexFormat.ELEMENT_RADIUS, new AttribProperties("Radius", false));
        ATTRIBUTES.put(GlintVertexFormat.ELEMENT_SIZE, new AttribProperties("Size", false));
        ATTRIBUTES.put(GlintVertexFormat.ELEMENT_OUTLINE_COLOR, new AttribProperties("OutlineColor", true));
    }

    private final VertexFormat format;
    private final int programId;
    private final Map<String, Uniform> uniforms = new HashMap<>();
    private final List<Uniform> samplerUnits = new ArrayList<>();

    public GlProgram(String name, VertexFormat format) {
        this(name, name, format);
    }

    public GlProgram(String vsh, String fsh, VertexFormat format) {
        this.format = format;

        int vertexShaderId;
        try (InputStream is = ResourceLoader.getStream("/assets/shaders/" + vsh + ".vsh")) {
            String vertexSource = IOUtils.toString(is, StandardCharsets.UTF_8);
            vertexShaderId = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load vertex shader source for " + vsh, e);
        }

        int fragmentShaderId;
        try (InputStream is = ResourceLoader.getStream("/assets/shaders/" + fsh + ".fsh")) {
            String fragmentSource = IOUtils.toString(is, StandardCharsets.UTF_8);
            fragmentShaderId = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fragment shader source for " + fsh, e);
        }

        this.programId = GL20.glCreateProgram();
        if (this.programId <= 0)
            throw new RuntimeException("Failed to create GL program (ID: " + this.programId + ")");

        GL20.glAttachShader(this.programId, vertexShaderId);
        GL20.glAttachShader(this.programId, fragmentShaderId);

        for (int i = 0; i < format.getElements().size(); i++) {
            VertexFormatElement element = format.getElements().get(i);
            AttribProperties props = ATTRIBUTES.get(element);
            if (props == null) {
                throw new RuntimeException("No attribute mapping for vertex format element: " + element);
            }
            GL20.glBindAttribLocation(this.programId, i, props.name);
        }

        GL20.glLinkProgram(this.programId);
        int linkStatus = GL20.glGetProgrami(this.programId, GL20.GL_LINK_STATUS);
        if (linkStatus == GL20.GL_FALSE) {
            String infoLog = GL20.glGetProgramInfoLog(this.programId);
            GL20.glDeleteProgram(this.programId);
            throw new RuntimeException("Failed to link GL program: " + infoLog);
        }
    }

    public void bind() {
        GL20.glUseProgram(programId);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public GlintVertexConsumer begin() {
        BufferBuilder b = RenderSystem.renderThreadTesselator().getBuilder();
        b.begin(GL11.GL_QUADS, format);
        return new GlintVertexConsumer(b);
    }

    public void draw() {
        BufferBuilder b = RenderSystem.renderThreadTesselator().getBuilder();
        b.end();
        Pair<BufferBuilder.DrawState, ByteBuffer> nextBuffer = b.popNextBuffer();
        nextBuffer.getSecond().clear();
        long pointer = MemoryUtil.memAddress(nextBuffer.getSecond());
        BufferBuilder.DrawState state = nextBuffer.getFirst();
        if (state.vertexCount() > 0) {
            int stride = format.getVertexSize();
            int offset = 0;
            for (int i = 0; i < format.getElements().size(); i++) {
                VertexFormatElement element = format.getElements().get(i);
                AttribProperties props = ATTRIBUTES.get(element);
                int count = element.getByteSize() / element.getType().getSize();
                GL20.glEnableVertexAttribArray(i);
                GL20.glVertexAttribPointer(i, count, element.getType().getGlType(), props.normalized, stride, pointer + offset);
                offset += element.getByteSize();
            }

            GlStateManager._drawArrays(state.mode(), 0, state.vertexCount());

            for (int i = 0; i < format.getElements().size(); i++) {
                GL20.glDisableVertexAttribArray(i);
            }
        }

        GlStateManager._activeTexture(GL20.GL_TEXTURE0);
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

    private static class AttribProperties {
        public final String name;
        public final boolean normalized;

        public AttribProperties(String name, boolean normalized) {
            this.name = name;
            this.normalized = normalized;
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
        private float[] mat4 = null;

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

        public void setMat4(float[] matrix) {
            if (this.mat4 == null || !Arrays.equals(this.mat4, matrix)) {
                this.mat4 = matrix.clone();
                GL20.glUniformMatrix4fv(location, false, this.mat4);
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
            GlStateManager._activeTexture(GL20.GL_TEXTURE0 + textureUnit);
            GlStateManager._bindTexture(textureId);
        }
    }
}
