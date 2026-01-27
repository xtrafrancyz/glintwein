package net.glintwein.ui.render.command;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.render.GlobalRender;
import net.glintwein.ui.render.shader.GlProgram;
import net.glintwein.ui.render.shader.GlintVertexConsumer;
import net.glintwein.ui.render.shader.Shaders;
import org.joml.Matrix3x2fc;

import java.util.List;

public class DrawRectCommand extends DrawCommand {
    private final Bounds bounds;
    private final Matrix3x2fc pose;
    private final float x0, y0, x1, y1;
    private final BorderRadius radius;
    private final int color;

    public DrawRectCommand(Matrix3x2fc pose, float x0, float y0, float x1, float y1, BorderRadius radius, int color) {
        this.bounds = Bounds.fromMinMax(x0, y0, x1, y1).transformMaxBounds(pose);
        this.pose = pose;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.radius = radius;
        this.color = color;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public boolean isSimilar(DrawCommand other) {
        if (!(other instanceof DrawRectCommand)) return false;
        DrawRectCommand o = (DrawRectCommand) other;
        return true; // All rects are similar for batching
    }

    public static class Executor implements DrawCommand.Executor<DrawRectCommand> {
        @Override
        public void execute(List<DrawRectCommand> commands) {
            GlProgram shader = Shaders.RECT;
            shader.bind();
            shader.getUniform("ProjMat").setMat4(GlobalRender.getGuiProxMatrix());
            GlintVertexConsumer consumer = shader.begin();
            for (DrawRectCommand cmd : commands) {
                vertex(consumer, cmd, cmd.x0, cmd.y0);
                vertex(consumer, cmd, cmd.x0, cmd.y1);
                vertex(consumer, cmd, cmd.x1, cmd.y1);
                vertex(consumer, cmd, cmd.x1, cmd.y0);
            }
            shader.draw();
        }

        private void vertex(GlintVertexConsumer consumer, DrawRectCommand cmd, float x, float y) {
            float width = cmd.x1 - cmd.x0;
            float height = cmd.y1 - cmd.y0;
            consumer.vertex2(cmd.pose, x, y)
                .color(cmd.color)
                .radius(cmd.radius, width, height)
                .size(width, height)
                .endVertex();
        }
    }
}
