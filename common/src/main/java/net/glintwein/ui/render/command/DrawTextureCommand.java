package net.glintwein.ui.render.command;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.GlobalRender;
import net.glintwein.ui.render.program.GlProgram;
import net.glintwein.ui.render.program.GlintVertexConsumer;
import org.joml.Matrix3x2fc;

import java.util.List;

public class DrawTextureCommand extends DrawCommand {
    private final Bounds bounds;
    private final Matrix3x2fc pose;
    private final float x0, y0, x1, y1;
    private final float u0, v0, u1, v1;
    private final BorderRadius radius;
    private final int textureId;
    private final int color;
    private final int outlineColor;
    private final float outlineWidth;

    public DrawTextureCommand(
        Matrix3x2fc pose,
        float x0, float y0, float x1, float y1,
        float u0, float v0, float u1, float v1,
        BorderRadius radius, int textureId, int color,
        int outlineColor, float outlineWidth
    ) {
        this.bounds = Bounds.fromMinMax(x0, y0, x1, y1).transformMaxBounds(pose);
        this.pose = pose;
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
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public boolean isSimilar(DrawCommand other) {
        if (!super.isSimilar(other)) return false;
        DrawTextureCommand cmd = (DrawTextureCommand) other;
        return this.textureId == cmd.textureId;
    }

    public static class Executor implements DrawCommand.Executor<DrawTextureCommand> {
        @Override
        public void execute(List<DrawTextureCommand> commands) {
            DrawTextureCommand first = commands.get(0);
            GlProgram program = GlProgram.RECT_TEXTURED;
            program.bind();
            program.getUniform("Texture").setTexture(first.textureId);
            program.getUniform("ProjMat").setMat4(GlobalRender.getGuiProxMatrix());
            GlintVertexConsumer consumer = program.begin();
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
            program.draw();
        }

        private void vertex(GlintVertexConsumer consumer, DrawTextureCommand cmd, float x, float y, float u, float v, float w, float h, int radiusPacked, float outlineWidth) {
            consumer.vertex2(cmd.pose, x, y)
                .color(cmd.color)
                .uv(u, v)
                .radius(radiusPacked)
                .size(w, h, outlineWidth)
                .color(cmd.outlineColor)
                .endVertex();
        }
    }
}
