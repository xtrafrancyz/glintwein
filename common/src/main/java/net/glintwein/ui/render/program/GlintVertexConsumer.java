package net.glintwein.ui.render.program;

import net.glintwein.ui.data.BorderRadius;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

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

    public GlintVertexConsumer color(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return color(r, g, b, a);
    }

    public GlintVertexConsumer color(int red, int green, int blue, int alpha) {
        builder.putByte(0, (byte) red);
        builder.putByte(1, (byte) green);
        builder.putByte(2, (byte) blue);
        builder.putByte(3, (byte) alpha);
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
        return (topLeft & 0xFF) | ((topRight & 0xFF) << 8) | ((bottomRight & 0xFF) << 16) | ((bottomLeft & 0xFF) << 24);
    }

    public GlintVertexConsumer radius(int packedRadius) {
        byte topLeft = (byte) (packedRadius & 0xFF);
        byte topRight = (byte) ((packedRadius >> 8) & 0xFF);
        byte bottomRight = (byte) ((packedRadius >> 16) & 0xFF);
        byte bottomLeft = (byte) ((packedRadius >> 24) & 0xFF);
        builder.putByte(0, topLeft);
        builder.putByte(1, topRight);
        builder.putByte(2, bottomRight);
        builder.putByte(3, bottomLeft);
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

    public void endVertex() {
        builder.endVertex();
    }
}
