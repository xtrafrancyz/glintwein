package net.glintwein.ui.render.command;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.glintwein.ui.render.GlobalRender;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.shader.GlProgram;
import net.glintwein.ui.render.shader.Shaders;

import java.util.List;

public class DrawTextCommand extends DrawCommand {
    private final GigaFont font;
    private final String text;
    private final float x;
    private final float y;
    private final float size;
    private final int color;

    private final Bounds bounds;

    public DrawTextCommand(GigaFont font, String text, float x, float y, float size, int color) {
        this.bounds = Bounds.fromXYWH(x, y, font.getWidth(text, size), font.getHeight(size));
        this.font = font;
        this.text = text;
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public boolean isSimilar(DrawCommand other) {
        if (!(other instanceof DrawTextCommand))
            return false;
        DrawTextCommand cmd = (DrawTextCommand) other;
        return this.font == cmd.font;
    }

    public static class Executor implements DrawCommand.Executor<DrawTextCommand> {
        @Override
        public void execute(List<DrawTextCommand> commands) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            DrawTextCommand first = commands.get(0);
            GlProgram msdf = Shaders.MSDF;
            msdf.bind();
            msdf.getUniform("Atlas").setTexture(first.font.getTextureId());
            msdf.getUniform("Range").setFloat(first.font.getPixelRange());
            msdf.getUniform("Thickness").setFloat(0f);
            msdf.getUniform("Smoothness").setFloat(0.5f);
            msdf.getUniform("Outline").setBool(false);
            msdf.getUniform("OutlineThickness").setFloat(0.3f);
            msdf.getUniform("OutlineColor").setColor4f(0xff000000);
            msdf.getUniform("ModelViewMat").setMat4(GlobalRender.getModelViewMatrix());
            msdf.getUniform("ProjMat").setMat4(GlobalRender.getGuiProxMatrix());
            VertexConsumer consumer = msdf.begin();
            for (DrawTextCommand cmd : commands) {
                cmd.font.render(consumer, cmd.text, cmd.x, cmd.y, cmd.size, cmd.color);
            }
            msdf.draw();
        }
    }
}
