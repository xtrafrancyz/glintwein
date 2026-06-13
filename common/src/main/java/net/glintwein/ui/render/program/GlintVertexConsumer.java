package net.glintwein.ui.render.program;

import net.glintwein.ui.data.BorderRadius;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

import java.util.function.Consumer;

public class GlintVertexConsumer {
    private static final Vector2f TEMP_VEC = new Vector2f();

    private final BufferBuilder builder;

    public GlintVertexConsumer(BufferBuilder builder) {
        this.builder = builder;
    }

    public GlintVertexConsumer vertex(Matrix3x2fc pose, float x, float y, float z) {
        Vector2f dest = pose.transformPosition(x, y, TEMP_VEC);
        builder.putFloat(0, dest.x);
        builder.putFloat(4, dest.y);
        builder.putFloat(8, z);
        builder.nextElement();
        return this;
    }

    public GlintVertexConsumer vertex2(Matrix3x2fc pose, float x, float y) {
        Vector2f dest = pose.transformPosition(x, y, TEMP_VEC);
        builder.putFloat(0, dest.x);
        builder.putFloat(4, dest.y);
        builder.nextElement();
        return this;
    }

    public GlintVertexConsumer color(int argb) {
        int abgr = ((argb & 0xFF000000)) | // A
            ((argb & 0x00FF0000) >> 16) | // R
            ((argb & 0x0000FF00)) |       // G
            ((argb & 0x000000FF) << 16);   // B
        // byte order is reversed in the shader
        builder.putInt(0, abgr);
        builder.nextElement();
        return this;
    }

    public GlintVertexConsumer uv(float u, float v) {
        builder.putFloat(0, u);
        builder.putFloat(4, v);
        builder.nextElement();
        return this;
    }

    public static int packRadius(BorderRadius radius, float scale, float width, float height) {
        float maxRadius = Math.min(width, height) / 2f;
        int topLeft = (int) Math.min(radius.topLeft * scale, maxRadius);
        int topRight = (int) Math.min(radius.topRight * scale, maxRadius);
        int bottomRight = (int) Math.min(radius.bottomRight * scale, maxRadius);
        int bottomLeft = (int) Math.min(radius.bottomLeft * scale, maxRadius);
        // byte order is reversed in the shader
        return (topLeft & 0xFF) | ((topRight & 0xFF) << 8) | ((bottomRight & 0xFF) << 16) | ((bottomLeft & 0xFF) << 24);
    }

    public GlintVertexConsumer radius(int packedRadius) {
        // in vec4 FragRadius; // x: top-left, y: top-right, z: bottom-right, w: bottom-left
        builder.putInt(0, packedRadius);
        builder.nextElement();
        return this;
    }

    public GlintVertexConsumer size(float width, float height, float outlineWidth) {
        builder.putFloat(0, width);
        builder.putFloat(4, height);
        builder.putFloat(8, outlineWidth);
        builder.nextElement();
        return this;
    }

    public GlintVertexConsumer customElement(Consumer<BufferBuilder> consumer) {
        consumer.accept(builder);
        builder.nextElement();
        return this;
    }

    public void endVertex() {
        builder.endVertex();
    }

    public BufferBuilder getBufferBuilder() {
        return builder;
    }
}
