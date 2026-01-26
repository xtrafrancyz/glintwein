package net.glintwein.ui.element;

import net.glintwein.ui.data.Size;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.font.Fonts;
import net.glintwein.ui.render.font.GigaFont;

import java.util.ArrayList;
import java.util.List;

public class Text extends LeafElement {
    private String text = "";
    private GigaFont font;
    private float fontSize = 16.0f;
    private int color = 0xFFFFFFFF;

    private List<WrappedLine> wrappedLines = new ArrayList<>();

    public Text(String text) {
        this.font = Fonts.REGULAR;

        setMeasureFunction((width, widthMode, height, heightMode) -> {
            // wrap content
            wrappedLines.clear();
            float contentWidth = 0;
            String[] lines = this.text.split("\n");
            if (widthMode != SizeMode.UNDEFINED) {
                List<String> wrapped = new ArrayList<>();
                for (String line : lines)
                    font.trimToWidth(line, fontSize, width, wrapped);
                for (String line : wrapped) {
                    float lineWidth = font.getWidth(line, fontSize);
                    wrappedLines.add(new WrappedLine(line, lineWidth));
                    contentWidth = Math.max(contentWidth, lineWidth);
                }
            } else {
                for (String line : lines) {
                    float lineWidth = font.getWidth(line, fontSize);
                    wrappedLines.add(new WrappedLine(line, lineWidth));
                    contentWidth = Math.max(contentWidth, lineWidth);
                }
            }
            float contentHeight = font.getHeight(fontSize) * wrappedLines.size();

            float measuredWidth;
            if (widthMode == SizeMode.EXACTLY) {
                measuredWidth = width;
            } else if (widthMode == SizeMode.AT_MOST) {
                measuredWidth = Math.min(contentWidth, width);
            } else {
                measuredWidth = contentWidth;
            }

            float measuredHeight;
            if (heightMode == SizeMode.EXACTLY)
                measuredHeight = height;
            else if (heightMode == SizeMode.AT_MOST)
                measuredHeight = Math.min(contentHeight, height);
            else
                measuredHeight = contentHeight;

            return new Size(measuredWidth, measuredHeight);
        });

        setText(text);
    }

    public void setFont(GigaFont font, float size) {
        if (this.font == font && this.fontSize == size)
            return;
        this.font = font;
        this.fontSize = size;
        markDirty();
    }

    public void setText(String text) {
        if (this.text.equals(text))
            return;
        this.text = text;
        markDirty();
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void draw(Context ctx) {
        super.draw(ctx);
        float y = contentBox.y;
        for (WrappedLine line : wrappedLines) {
            ctx.drawText(font, line.text, contentBox.x, y, fontSize, color);
            y += font.getHeight(fontSize);
        }
    }

    private static class WrappedLine {
        String text;
        float width;

        WrappedLine(String text, float width) {
            this.text = text;
            this.width = width;
        }
    }
}
