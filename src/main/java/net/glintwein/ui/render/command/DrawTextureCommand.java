package net.glintwein.ui.render.command;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.GlobalRender;
import net.glintwein.ui.render.shader.GlProgram;
import net.glintwein.ui.render.shader.GlintVertexConsumer;
import net.glintwein.ui.render.shader.Shaders;
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
            GlProgram shader = Shaders.RECT_TEXTURED;
            shader.bind();
            shader.getUniform("Texture").setTexture(first.textureId);
            shader.getUniform("ProjMat").setMat4(GlobalRender.getGuiProxMatrix());
            GlintVertexConsumer consumer = shader.begin();
            for (DrawTextureCommand cmd : commands) {
                vertex(consumer, cmd, cmd.x0, cmd.y0, cmd.u0, cmd.v0);
                vertex(consumer, cmd, cmd.x0, cmd.y1, cmd.u0, cmd.v1);
                vertex(consumer, cmd, cmd.x1, cmd.y1, cmd.u1, cmd.v1);
                vertex(consumer, cmd, cmd.x1, cmd.y0, cmd.u1, cmd.v0);
            }
            shader.draw();
        }

        private void vertex(GlintVertexConsumer consumer, DrawTextureCommand cmd, float x, float y, float u, float v) {
            float width = cmd.x1 - cmd.x0;
            float height = cmd.y1 - cmd.y0;
            consumer.vertex2(cmd.pose, x, y)
                .color(cmd.color)
                .uv(u, v)
                .radius(cmd.radius, width, height)
                .size(width, height, cmd.outlineWidth)
                .color(cmd.outlineColor)
                .endVertex();
        }
    }
}
