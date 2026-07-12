package net.glintwein.ui.render.command;

import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.program.GlProgram;
import net.glintwein.ui.render.program.GlintVertexConsumer;
import net.glintwein.ui.util.ARGB;
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

    public DrawTextureCommand() {
        this.bounds = Bounds.empty();
        this.pose = new Matrix3x2f();
    }

    public DrawTextureCommand set(
        Matrix3x2fc pose,
        float x0, float y0, float x1, float y1,
        float u0, float v0, float u1, float v1,
        BorderRadius radius, int textureId, int color,
        int outlineColor, float outlineWidth
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
        this.color = color;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
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
                float w = (cmd.x1 - cmd.x0) * sx;
                float h = (cmd.y1 - cmd.y0) * sy;
                float avg = (sx + sy) / 2.0f;
                int radiusPacked = GlintVertexConsumer.packRadius(cmd.radius, avg, w, h);
                float outlineWidth = cmd.outlineWidth * avg;

                vertex(consumer, cmd, cmd.x0, cmd.y0, cmd.u0, cmd.v0, w, h, radiusPacked, outlineWidth);
                vertex(consumer, cmd, cmd.x0, cmd.y1, cmd.u0, cmd.v1, w, h, radiusPacked, outlineWidth);
                vertex(consumer, cmd, cmd.x1, cmd.y1, cmd.u1, cmd.v1, w, h, radiusPacked, outlineWidth);
                vertex(consumer, cmd, cmd.x1, cmd.y0, cmd.u1, cmd.v0, w, h, radiusPacked, outlineWidth);
            }
        }

        private static void vertex(GlintVertexConsumer consumer, DrawTextureCommand cmd, float x, float y, float u, float v, float w, float h, int radiusPacked, float outlineWidth) {
            consumer.vertex2(cmd.pose, x, y)
                .color(ARGB.premulAlpha(cmd.color))
                .uv(u, v)
                .radius(radiusPacked)
                .size(w, h, outlineWidth)
                .color(cmd.outlineColor)
                .endVertex();
        }
    }
}
