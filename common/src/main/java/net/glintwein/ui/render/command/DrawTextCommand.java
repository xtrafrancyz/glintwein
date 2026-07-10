package net.glintwein.ui.render.command;

import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.program.GlProgram;
import net.glintwein.ui.render.program.GlintVertexConsumer;
import net.glintwein.ui.util.ARGB;
import net.glintwein.util.PerFrameObjectPool;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

import java.util.List;

public class DrawTextCommand extends DrawCommand {
    public static final PerFrameObjectPool<DrawTextCommand> POOL = new PerFrameObjectPool<>(
        DrawTextCommand::new,
        DrawTextCommand::reset
    );

    private final Matrix3x2f pose;
    private GigaFont font;
    private String text;
    private float x;
    private float y;
    private float size;
    private int colorTL, colorTR, colorBR, colorBL;
    private boolean solidColor;
    private float outlineWidth;
    private int outlineColor;

    public DrawTextCommand() {
        this.bounds = Bounds.empty();
        this.pose = new Matrix3x2f();
    }

    public DrawTextCommand set(Matrix3x2fc pose, GigaFont font, String text, float x, float y, float size, int color) {
        return set(pose, font, text, x, y, size, color, color, color, color, 0, 0f);
    }

    public DrawTextCommand set(
        Matrix3x2fc pose, GigaFont font, String text,
        float x, float y, float size,
        int colorTL, int colorTR, int colorBR, int colorBL,
        int outlineColor, float outlineWidth
    ) {
        this.pose.set(pose);
        this.bounds.setXYWH(x, y, font.getWidth(text, size), font.getHeight(size)).transformMaxBounds(pose);
        this.font = font;
        this.text = text;
        this.x = x;
        this.y = y;
        this.size = size;
        this.colorTL = colorTL;
        this.colorTR = colorTR;
        this.colorBR = colorBR;
        this.colorBL = colorBL;
        this.solidColor = (colorTL == colorTR) && (colorTL == colorBR) && (colorTL == colorBL);
        this.outlineWidth = Math.max(0, outlineWidth);
        this.outlineColor = this.outlineWidth <= 0 ? 0 : outlineColor;
        return this;
    }

    @Override
    public boolean isSimilar(DrawCommand other) {
        if (!super.isSimilar(other)) return false;
        DrawTextCommand cmd = (DrawTextCommand) other;
        return this.font == cmd.font && this.outlineWidth == cmd.outlineWidth && this.outlineColor == cmd.outlineColor;
    }

    @Override
    public void release() {
        POOL.release(this);
    }

    @Override
    public void reset() {
        super.reset();
        font = null;
    }

    public static class Executor implements DrawCommand.Executor<DrawTextCommand> {
        @Override
        public void execute(List<DrawTextCommand> commands) {
            DrawTextCommand first = commands.get(0);
            GlProgram msdf = GlProgram.MSDF;
            msdf.bind();
            msdf.getUniform("Atlas").setTexture(first.font.getTextureId());
            msdf.getUniform("Range").setFloat(first.font.getPixelRange());
            msdf.getUniform("Thickness").setFloat(0f);
            msdf.getUniform("Smoothness").setFloat(0.5f);
            msdf.getUniform("OutlineThickness").setFloat(first.outlineWidth);
            msdf.getUniform("OutlineColor").setColor4f(first.outlineColor);
            msdf.getUniform("ProjMat").setMat4(GlobalUIState.getGuiProjectionMatrix());
            GlintVertexConsumer consumer = msdf.begin();
            for (DrawTextCommand cmd : commands) {
                if (cmd.solidColor)
                    cmd.font.render(consumer, cmd.pose, cmd.text, cmd.x, cmd.y, cmd.size, ARGB.premulAlpha(cmd.colorTL));
                else
                    cmd.font.renderGlyphColor(
                        consumer, cmd.pose, cmd.text, cmd.x, cmd.y, cmd.size,
                        ARGB.premulAlpha(cmd.colorTL), ARGB.premulAlpha(cmd.colorTR),
                        ARGB.premulAlpha(cmd.colorBR), ARGB.premulAlpha(cmd.colorBL)
                    );
            }
            msdf.draw();
        }
    }
}
