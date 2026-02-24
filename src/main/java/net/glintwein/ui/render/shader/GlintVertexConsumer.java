package net.glintwein.ui.render.shader;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.glintwein.ui.data.BorderRadius;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

public class GlintVertexConsumer implements VertexConsumer {
    private static final Vector2f TEMP_VEC = new Vector2f();

    private final BufferBuilder builder;

    public GlintVertexConsumer(BufferBuilder builder) {
        this.builder = builder;
    }

    @Override
    public GlintVertexConsumer vertex(double x, double y, double z) {
        builder.vertex(x, y, z);
        return this;
    }

    public GlintVertexConsumer vertex(Matrix3x2fc pose, float x, float y, float z) {
        Vector2f dest = pose.transformPosition(x, y, TEMP_VEC);
        builder.vertex(dest.x, dest.y, z);
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
        builder.color(r, g, b, a);
        return this;
    }

    @Override
    public GlintVertexConsumer color(int red, int green, int blue, int alpha) {
        builder.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public GlintVertexConsumer uv(float u, float v) {
        builder.uv(u, v);
        return this;
    }

    @Override
    public GlintVertexConsumer overlayCoords(int u, int v) {
        builder.overlayCoords(u, v);
        return this;
    }

    @Override
    public GlintVertexConsumer uv2(int u, int v) {
        builder.uv2(u, v);
        return this;
    }

    @Override
    public GlintVertexConsumer normal(float x, float y, float z) {
        builder.normal(x, y, z);
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

    @Override
    public void endVertex() {
        builder.endVertex();
    }
}
