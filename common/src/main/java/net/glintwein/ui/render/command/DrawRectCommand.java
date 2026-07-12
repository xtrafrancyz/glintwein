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

public class DrawRectCommand extends DrawCommand {
    public static final PerFrameObjectPool<DrawRectCommand> POOL = new PerFrameObjectPool<>(
        DrawRectCommand::new,
        DrawRectCommand::reset
    );

    private final Matrix3x2f pose;
    private float x0, y0, x1, y1;
    private BorderRadius radius;
    private int colorTL, colorTR, colorBR, colorBL;
    private int outlineColor;
    private float outlineWidth;

    public DrawRectCommand() {
        this.bounds = Bounds.empty();
        this.pose = new Matrix3x2f();
    }

    public DrawRectCommand set(Matrix3x2fc pose, float x0, float y0, float x1, float y1, BorderRadius radius, int color, int outlineColor, float outlineWidth) {
        return set(pose, x0, y0, x1, y1, radius, color, color, color, color, outlineColor, outlineWidth);
    }

    public DrawRectCommand set(Matrix3x2fc pose, float x0, float y0, float x1, float y1, BorderRadius radius, int colorTL, int colorTR, int colorBR, int colorBL, int outlineColor, float outlineWidth) {
        this.bounds.set(x0, y0, x1, y1).transformMaxBounds(pose);
        this.pose.set(pose);
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.radius = radius;
        this.colorTL = colorTL;
        this.colorTR = colorTR;
        this.colorBR = colorBR;
        this.colorBL = colorBL;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
        return this;
    }

    @Override
    public void release() {
        POOL.release(this);
    }

    public static class Executor extends DrawCommand.SimpleExecutor<DrawRectCommand> {
        public Executor() {
            super(GlProgram.RECT);
        }

        @Override
        protected void bindUniforms(GlProgram program, DrawRectCommand first) {
            program.getUniform("ProjMat").setMat4(GlobalUIState.getGuiProjectionMatrix());
        }

        @Override
        protected void buildVertexBuffer(GlintVertexConsumer consumer, List<DrawRectCommand> commands) {
            for (DrawRectCommand cmd : commands) {
                float sx = (float) Math.sqrt(cmd.pose.m00() * cmd.pose.m00() + cmd.pose.m01() * cmd.pose.m01());
                float sy = (float) Math.sqrt(cmd.pose.m10() * cmd.pose.m10() + cmd.pose.m11() * cmd.pose.m11());
                float w = (cmd.x1 - cmd.x0) * sx;
                float h = (cmd.y1 - cmd.y0) * sy;
                float avg = (sx + sy) / 2.0f;
                int radiusPacked = GlintVertexConsumer.packRadius(cmd.radius, avg, w, h);
                float outlineWidth = cmd.outlineWidth * avg;

                vertex(consumer, cmd, cmd.x0, cmd.y0, cmd.colorTL, w, h, radiusPacked, outlineWidth);
                vertex(consumer, cmd, cmd.x0, cmd.y1, cmd.colorBL, w, h, radiusPacked, outlineWidth);
                vertex(consumer, cmd, cmd.x1, cmd.y1, cmd.colorBR, w, h, radiusPacked, outlineWidth);
                vertex(consumer, cmd, cmd.x1, cmd.y0, cmd.colorTR, w, h, radiusPacked, outlineWidth);
            }
        }

        private static void vertex(GlintVertexConsumer consumer, DrawRectCommand cmd, float x, float y, int color, float w, float h, int radiusPacked, float outlineWidth) {
            consumer.vertex2(cmd.pose, x, y)
                .color(ARGB.premulAlpha(color))
                .radius(radiusPacked)
                .size(w, h, outlineWidth)
                .color(cmd.outlineColor)
                .endVertex();
        }
    }
}
