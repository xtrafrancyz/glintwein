package net.glintwein.ui.render.command;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.GlobalRender;
import net.glintwein.ui.render.program.GlProgram;
import net.glintwein.ui.render.program.GlintVertexConsumer;
import org.joml.Matrix3x2fc;

import java.util.List;

public class DrawRectCommand extends DrawCommand {
    private final Matrix3x2fc pose;
    private final float x0, y0, x1, y1;
    private final BorderRadius radius;
    private final int colorTL, colorTR, colorBR, colorBL;
    private final int outlineColor;
    private final float outlineWidth;

    public DrawRectCommand(Matrix3x2fc pose, float x0, float y0, float x1, float y1, BorderRadius radius, int color, int outlineColor, float outlineWidth) {
        this(pose, x0, y0, x1, y1, radius, color, color, color, color, outlineColor, outlineWidth);
    }

    public DrawRectCommand(Matrix3x2fc pose, float x0, float y0, float x1, float y1, BorderRadius radius, int colorTL, int colorTR, int colorBR, int colorBL, int outlineColor, float outlineWidth) {
        this.bounds = Bounds.fromMinMax(x0, y0, x1, y1).transformMaxBounds(pose);
        this.pose = pose;
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
    }

    public static class Executor implements DrawCommand.Executor<DrawRectCommand> {
        @Override
        public void execute(List<DrawRectCommand> commands) {
            GlProgram program = GlProgram.RECT;
            program.bind();
            program.getUniform("ProjMat").setMat4(GlobalRender.getGuiProxMatrix());
            GlintVertexConsumer consumer = program.begin();
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
            program.draw();
        }

        private void vertex(GlintVertexConsumer consumer, DrawRectCommand cmd, float x, float y, int color, float w, float h, int radiusPacked, float outlineWidth) {
            consumer.vertex2(cmd.pose, x, y)
                .color(color)
                .radius(radiusPacked)
                .size(w, h, outlineWidth)
                .color(cmd.outlineColor)
                .endVertex();
        }
    }
}
