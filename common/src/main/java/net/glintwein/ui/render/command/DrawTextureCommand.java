package net.glintwein.ui.render.command;

import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.data.NineSliceSpec;
import net.glintwein.ui.render.program.GlProgram;
import net.glintwein.ui.render.program.GlintVertexConsumer;
import net.glintwein.ui.util.ARGB;
import net.glintwein.ui.util.GMath;
import net.glintwein.util.PerFrameObjectPool;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

import java.util.List;

public class DrawTextureCommand extends DrawCommand {
    public static final PerFrameObjectPool<DrawTextureCommand> POOL = new PerFrameObjectPool<>(
        DrawTextureCommand::new,
        DrawTextureCommand::reset
    );

    private final Matrix3x2f pose;
    private float x0, y0, x1, y1;
    private float u0, v0, u1, v1;
    private BorderRadius radius;
    private int textureId;
    private int color;
    private int outlineColor;
    private float outlineWidth;
    private NineSliceSpec sliceSpec;

    public DrawTextureCommand() {
        this.bounds = Bounds.empty();
        this.pose = new Matrix3x2f();
    }

    public DrawTextureCommand set(
        Matrix3x2fc pose,
        float x0, float y0, float x1, float y1,
        float u0, float v0, float u1, float v1,
        BorderRadius radius, int textureId, int color,
        int outlineColor, float outlineWidth,
        NineSliceSpec sliceSpec
    ) {
        this.bounds.set(x0, y0, x1, y1).transformMaxBounds(pose);
        this.pose.set(pose);
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.radius = radius;
        this.textureId = textureId;
        this.color = ARGB.premulAlpha(color);
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
        this.sliceSpec = sliceSpec;
        return this;
    }

    @Override
    public boolean isSimilar(DrawCommand other) {
        if (!super.isSimilar(other)) return false;
        DrawTextureCommand cmd = (DrawTextureCommand) other;
        return this.textureId == cmd.textureId;
    }

    @Override
    public void release() {
        POOL.release(this);
    }

    public static class Executor extends DrawCommand.SimpleExecutor<DrawTextureCommand> {
        public Executor() {
            super(GlProgram.RECT_TEXTURED);
        }

        @Override
        protected void bindUniforms(GlProgram program, DrawTextureCommand first) {
            program.getUniform("Texture").setTexture(first.textureId);
            program.getUniform("ProjMat").setMat4(GlobalUIState.getGuiProjectionMatrix());
        }

        @Override
        protected void buildVertexBuffer(GlintVertexConsumer consumer, List<DrawTextureCommand> commands) {
            for (DrawTextureCommand cmd : commands) {
                float sx = (float) Math.sqrt(cmd.pose.m00() * cmd.pose.m00() + cmd.pose.m01() * cmd.pose.m01());
                float sy = (float) Math.sqrt(cmd.pose.m10() * cmd.pose.m10() + cmd.pose.m11() * cmd.pose.m11());
                float avg = (sx + sy) / 2.0f;
                cmd.outlineWidth *= avg;

                if (cmd.sliceSpec == null) {
                    float w = (cmd.x1 - cmd.x0) * sx;
                    float h = (cmd.y1 - cmd.y0) * sy;
                    quad(
                        consumer, cmd,
                        cmd.x0, cmd.y0, cmd.x1, cmd.y1,
                        cmd.u0, cmd.v0, cmd.u1, cmd.v1,
                        w, h,
                        GlintVertexConsumer.packRadius(cmd.radius, avg, w, h)
                    );
                } else {
                    float width = cmd.x1 - cmd.x0;
                    float height = cmd.y1 - cmd.y0;

                    boolean tile = cmd.sliceSpec.type() == NineSliceSpec.Type.TILE;
                    float scale = cmd.sliceSpec.scale();
                    int leftT = cmd.sliceSpec.left();
                    int topT = cmd.sliceSpec.top();
                    int rightT = cmd.sliceSpec.right();
                    int bottomT = cmd.sliceSpec.bottom();

                    float left = leftT * scale;
                    float top = topT * scale;
                    float right = rightT * scale;
                    float bottom = bottomT * scale;
                    float flexWidth = width - left - right;
                    float flexHeight = height - top - bottom;
                    if (flexWidth < 0) {
                        float xTexCut = -flexWidth / (left + right);
                        left *= 1 - xTexCut;
                        right *= 1 - xTexCut;
                        flexWidth = 0;
                    }
                    if (flexHeight < 0) {
                        float yTexCut = -flexHeight / (top + bottom);
                        top *= 1 - yTexCut;
                        bottom *= 1 - yTexCut;
                        flexHeight = 0;
                    }
                    float texWidth = cmd.sliceSpec.textureWidth() * scale;
                    float texHeight = cmd.sliceSpec.textureHeight() * scale;
                    float flexTexWidth = texWidth - left - right;
                    float flexTexHeight = texHeight - top - bottom;
                    float leftU = GMath.lerp(left / texWidth, cmd.u0, cmd.u1);
                    float topV = GMath.lerp(top / texHeight, cmd.v0, cmd.v1);
                    float rightU = GMath.lerp(1.0F - right / texWidth, cmd.u0, cmd.u1);
                    float bottomV = GMath.lerp(1.0F - bottom / texHeight, cmd.v0, cmd.v1);
                    float halfPxX = 0.5f / sx;
                    float halfPxY = 0.5f / sy;
                    float halfPxU = 0.25f / texWidth;
                    float halfPxV = 0.25f / texHeight;
                    BorderRadius temp = new BorderRadius();

                    // Top-left
                    if (left > 0 && top > 0)
                        quad(consumer, cmd,
                            cmd.x0, cmd.y0, cmd.x0 + left + halfPxX, cmd.y0 + top + halfPxY,
                            cmd.u0, cmd.v0, leftU + halfPxU, topV + halfPxV,
                            left * sx, top * sy,
                            GlintVertexConsumer.packRadius(temp.set(0).topLeft(cmd.radius.topLeft), avg, left * sx, top * sy)
                        );
                    // Top
                    if (flexWidth > 0 && top > 0)
                        quadsRepeating(consumer, cmd,
                            cmd.x0 + left, cmd.y0, cmd.x1 - right, cmd.y0 + top,
                            leftU, cmd.v0, rightU, topV,
                            flexTexWidth, top,
                            sx, sy, tile,
                            halfPxX, halfPxY, halfPxU, halfPxV
                        );
                    // Top-right
                    if (right > 0 && top > 0)
                        quad(consumer, cmd,
                            cmd.x1 - right, cmd.y0, cmd.x1, cmd.y0 + top + halfPxY,
                            rightU, cmd.v0, cmd.u1, topV + halfPxV,
                            right * sx, top * sy,
                            GlintVertexConsumer.packRadius(temp.set(0).topRight(cmd.radius.topRight), avg, right * sx, top * sy)
                        );
                    // Left
                    if (left > 0 && flexHeight > 0)
                        quadsRepeating(consumer, cmd,
                            cmd.x0, cmd.y0 + top, cmd.x0 + left, cmd.y1 - bottom,
                            cmd.u0, topV, leftU, bottomV,
                            left, flexTexHeight,
                            sx, sy, tile,
                            halfPxX, halfPxY, halfPxU, halfPxV
                        );
                    // Center
                    if (flexWidth > 0 && flexHeight > 0)
                        quadsRepeating(consumer, cmd,
                            cmd.x0 + left, cmd.y0 + top, cmd.x1 - right, cmd.y1 - bottom,
                            leftU, topV, rightU, bottomV,
                            flexTexWidth, flexTexHeight,
                            sx, sy, tile,
                            halfPxX, halfPxY, halfPxU, halfPxV
                        );
                    // Right
                    if (right > 0 && flexHeight > 0)
                        quadsRepeating(consumer, cmd,
                            cmd.x1 - right, cmd.y0 + top, cmd.x1, cmd.y1 - bottom,
                            rightU, topV, cmd.u1, bottomV,
                            right, flexTexHeight,
                            sx, sy, tile,
                            0, halfPxY, 0, halfPxV
                        );
                    // Bottom-left
                    if (left > 0 && bottom > 0)
                        quad(consumer, cmd,
                            cmd.x0, cmd.y1 - bottom, cmd.x0 + left + halfPxX, cmd.y1,
                            cmd.u0, bottomV, leftU + halfPxU, cmd.v1,
                            left * sx, bottom * sy,
                            GlintVertexConsumer.packRadius(temp.set(0).bottomLeft(cmd.radius.bottomLeft), avg, left * sx, bottom * sy)
                        );
                    // Bottom
                    if (flexWidth > 0 && bottom > 0)
                        quadsRepeating(consumer, cmd,
                            cmd.x0 + left, cmd.y1 - bottom, cmd.x1 - right, cmd.y1,
                            leftU, bottomV, rightU, cmd.v1,
                            flexTexWidth, bottom,
                            sx, sy, tile,
                            halfPxX, 0, halfPxU, 0
                        );
                    // Bottom-right
                    if (right > 0 && bottom > 0)
                        quad(consumer, cmd,
                            cmd.x1 - right, cmd.y1 - bottom, cmd.x1, cmd.y1,
                            rightU, bottomV, cmd.u1, cmd.v1,
                            right * sx, bottom * sy,
                            GlintVertexConsumer.packRadius(temp.set(0).bottomRight(cmd.radius.bottomRight), avg, right * sx, bottom * sy)
                        );
                }
            }
        }

        private static void quadsRepeating(
            GlintVertexConsumer consumer, DrawTextureCommand cmd,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            float texWidth, float texHeight,
            float sx, float sy, boolean tile,
            float halfPxX, float halfPxY, float halfPxU, float halfPxV
        ) {
            if (!tile) {
                quad(consumer, cmd, x0, y0, x1 + halfPxX, y1 + halfPxY, u0, v0, u1 + halfPxU, v1 + halfPxV, (x1 - x0) * sx, (y1 - y0) * sy, 0);
                return;
            }

            for (float xOffset = x0; xOffset < x1; xOffset += texWidth) {
                float sliceWidth = Math.min(texWidth, x1 - xOffset);
                for (float yOffset = y0; yOffset < y1; yOffset += texHeight) {
                    float sliceHeight = Math.min(texHeight, y1 - yOffset);
                    quad(
                        consumer, cmd,
                        xOffset, yOffset, xOffset + sliceWidth + halfPxX, yOffset + sliceHeight + halfPxY,
                        u0, v0, GMath.lerp(sliceWidth / texWidth, u0, u1) + halfPxU, GMath.lerp(sliceHeight / texHeight, v0, v1) + halfPxV,
                        sliceWidth * sx, sliceHeight * sy, 0
                    );
                }
            }
        }

        private static void quad(
            GlintVertexConsumer consumer, DrawTextureCommand cmd,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            float w, float h,
            int radiusPacked
        ) {
            vertex(consumer, cmd, x0, y0, u0, v0, w, h, radiusPacked);
            vertex(consumer, cmd, x0, y1, u0, v1, w, h, radiusPacked);
            vertex(consumer, cmd, x1, y1, u1, v1, w, h, radiusPacked);
            vertex(consumer, cmd, x1, y0, u1, v0, w, h, radiusPacked);
        }

        private static void vertex(GlintVertexConsumer consumer, DrawTextureCommand cmd, float x, float y, float u, float v, float w, float h, int radiusPacked) {
            consumer.vertex2(cmd.pose, x, y)
                .color(cmd.color)
                .uv(u, v)
                .radius(radiusPacked)
                .size(w, h, cmd.outlineWidth)
                .color(cmd.outlineColor)
                .endVertex();
        }
    }
}
