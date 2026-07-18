package net.glintwein.ui.rtf;

import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawTextBuilder;

public class RenderTokenText implements RenderToken {
    private final String text;
    private final TextSpan.Style style;
    private final float width;

    public RenderTokenText(String text, TextSpan.Style style) {
        this.text = text;
        this.style = style;
        this.width = style.font.getWidth(text, style.fontSize);
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return style.font.getHeight(style.fontSize);
    }

    @Override
    public void draw(Context ctx, float x, float y) {
        if (style.outlineWidth > 0) {
            ctx.drawText(DrawTextBuilder.of(text, style.font, style.fontSize)
                .offset(x, y)
                .color(style.color)
                .outline(style.outlineColor, style.outlineWidth)
            );
        } else {
            ctx.drawText(style.font, text, x, y, style.fontSize, style.color);
        }
    }

    @Override
    public RenderToken tryMergeNext(RenderToken next) {
        if (next instanceof RenderTokenSpace) {
            if (next.getWidth() == style.font.getSpaceWidth(style.fontSize))
                return new RenderTokenText(text + " ", style);
        } else if (next instanceof RenderTokenText) {
            RenderTokenText nextText = (RenderTokenText) next;
            if (nextText.style.equals(this.style))
                return new RenderTokenText(text + nextText.text, style);
        }
        return null;
    }
}
