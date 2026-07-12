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

public class DrawShadowCommand extends DrawCommand {
    public static final PerFrameObjectPool<DrawShadowCommand> POOL = new PerFrameObjectPool<>(
        DrawShadowCommand::new,
        DrawShadowCommand::reset
    );

    private final Matrix3x2f pose;
    private float x0, y0, x1, y1;
    private BorderRadius radius;
    private int colorTL, colorTR, colorBR, colorBL;
    private float blurSpread;

    public DrawShadowCommand() {
        pose = new Matrix3x2f();
        bounds = Bounds.empty();
    }

    public DrawShadowCommand set(Matrix3x2fc pose, float x0, float y0, float x1, float y1, BorderRadius radius, int colorTL, int colorTR, int colorBR, int colorBL, float blurSpread) {
        blurSpread /= 2.0f;
        this.x0 = x0 - blurSpread;
        this.y0 = y0 - blurSpread;
        this.x1 = x1 + blurSpread;
        this.y1 = y1 + blurSpread;
        this.bounds.set(this.x0, this.y0, this.x1, this.y1).transformMaxBounds(pose);
        this.pose.set(pose);
        this.radius = radius;
        this.colorTL = colorTL;
        this.colorTR = colorTR;
        this.colorBR = colorBR;
        this.colorBL = colorBL;
        this.blurSpread = blurSpread;
        return this;
    }

    @Override
    public void release() {
        POOL.release(this);
    }

    public static class Executor extends SimpleExecutor<DrawShadowCommand> {
        public Executor() {
            super(GlProgram.RECT_SHADOW);
        }

        @Override
        protected void bindUniforms(GlProgram program, DrawShadowCommand first) {
            program.getUniform("ProjMat").setMat4(GlobalUIState.getGuiProjectionMatrix());
        }

        @Override
        protected void buildVertexBuffer(GlintVertexConsumer consumer, List<DrawShadowCommand> commands) {
            for (DrawShadowCommand cmd : commands) {
                float sx = (float) Math.sqrt(cmd.pose.m00() * cmd.pose.m00() + cmd.pose.m01() * cmd.pose.m01());
                float sy = (float) Math.sqrt(cmd.pose.m10() * cmd.pose.m10() + cmd.pose.m11() * cmd.pose.m11());
                float avg = (sx + sy) / 2.0f;
                float spread = cmd.blurSpread * avg;
                float w = (cmd.x1 - cmd.x0 - cmd.blurSpread * 2) * sx;
                float h = (cmd.y1 - cmd.y0 - cmd.blurSpread * 2) * sy;
                int radiusPacked = GlintVertexConsumer.packRadius(cmd.radius, avg, w, h);

                consumer.vertex2(cmd.pose, cmd.x0, cmd.y0)
                    .color(ARGB.premulAlpha(cmd.colorTL))
                    .radius(radiusPacked)
                    .size(w, h, spread)
                    .endVertex();
                consumer.vertex2(cmd.pose, cmd.x0, cmd.y1)
                    .color(ARGB.premulAlpha(cmd.colorBL))
                    .radius(radiusPacked)
                    .size(w, h, spread)
                    .endVertex();
                consumer.vertex2(cmd.pose, cmd.x1, cmd.y1)
                    .color(ARGB.premulAlpha(cmd.colorBR))
                    .radius(radiusPacked)
                    .size(w, h, spread)
                    .endVertex();
                consumer.vertex2(cmd.pose, cmd.x1, cmd.y0)
                    .color(ARGB.premulAlpha(cmd.colorTR))
                    .radius(radiusPacked)
                    .size(w, h, spread)
                    .endVertex();
            }
        }
    }
}
