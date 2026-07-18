package net.glintwein.ui.element;

import net.glintwein.platform.YogaMeasureFunction;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.Size;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.font.SizedFont;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Text extends LeafElement {
    private String text = "";
    protected SizedFont font;
    protected int color = 0xFFFFFFFF;
    private WrapMode wrapMode = WrapMode.WORD;
    private Align align = Align.LEFT;

    private final List<WrappedLine> wrappedLines = new ArrayList<>();

    public Text() {
        this.font = GlobalUIState.getDefaultTextFont();
        setMeasureFunction(this::wrapAndMeasure);
    }

    public Text(String text) {
        this();
        setText(text);
    }

    private Size wrapAndMeasure(float width, YogaMeasureFunction.SizeMode widthMode,
                                float height, YogaMeasureFunction.SizeMode heightMode) {
        // wrap content
        wrappedLines.clear();
        float contentWidth = 0;
        if (widthMode != YogaMeasureFunction.SizeMode.UNDEFINED && wrapMode == WrapMode.WORD) {
            List<String> wrapped = new ArrayList<>();
            font.wrapText(this.text, width, wrapped);
            for (String line : wrapped) {
                float lineWidth = font.getWidth(line);
                wrappedLines.add(new WrappedLine(wrappedLines.size(), line, lineWidth));
                contentWidth = Math.max(contentWidth, lineWidth);
            }
        } else {
            for (String line : this.text.split("\n", -1)) {
                float lineWidth = font.getWidth(line);
                wrappedLines.add(new WrappedLine(wrappedLines.size(), line, lineWidth));
                contentWidth = Math.max(contentWidth, lineWidth);
            }
        }
        if (wrappedLines.isEmpty())
            wrappedLines.add(new WrappedLine(0, "", 0));
        float contentHeight = font.getHeight() * wrappedLines.size();

        float measuredWidth;
        if (widthMode == YogaMeasureFunction.SizeMode.EXACTLY) {
            measuredWidth = width;
        } else if (widthMode == YogaMeasureFunction.SizeMode.AT_MOST) {
            measuredWidth = Math.min(contentWidth, width);
        } else {
            measuredWidth = contentWidth;
        }

        float measuredHeight;
        if (heightMode == YogaMeasureFunction.SizeMode.EXACTLY)
            measuredHeight = height;
        else if (heightMode == YogaMeasureFunction.SizeMode.AT_MOST)
            measuredHeight = Math.min(contentHeight, height);
        else
            measuredHeight = contentHeight;

        return new Size(measuredWidth, measuredHeight);
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public void setFont(GigaFont font, float size) {
        if (this.font.font() == font && this.font.size() == size)
            return;
        this.font = new SizedFont(font, size);
        markDirty();
    }

    public void setFont(SizedFont font) {
        if (this.font.font() == font.font() && this.font.size() == font.size())
            return;
        this.font = font;
        markDirty();
    }

    public SizedFont getFont() {
        return font;
    }

    public void setText(String text) {
        if (this.text.equals(text))
            return;
        this.text = text;
        markDirty();
    }

    public String getText() {
        return text;
    }

    public void setWrapMode(WrapMode mode) {
        if (this.wrapMode == mode)
            return;
        this.wrapMode = mode;
        markDirty();
    }

    public void setColor(int color) {
        this.color = color;
    }

    protected int getTextColor() {
        return color;
    }

    protected List<WrappedLine> getWrappedLines() {
        if (wrappedLines.isEmpty())
            return Collections.singletonList(new WrappedLine(0, "", 0));
        return wrappedLines;
    }

    @Override
    protected void readYogaLayout() {
        super.readYogaLayout();
        if (wrappedLines.isEmpty()) {
            wrapAndMeasure(
                contentBox.width, YogaMeasureFunction.SizeMode.EXACTLY,
                contentBox.height, YogaMeasureFunction.SizeMode.EXACTLY
            );
        }
    }

    @Override
    protected void markDirty() {
        super.markDirty();
        wrappedLines.clear();
    }

    @Override
    protected void drawContent(Context ctx) {
        for (WrappedLine line : getWrappedLines())
            drawLine(ctx, line);
    }

    protected void drawLine(Context ctx, WrappedLine line) {
        ctx.drawText(font.font(), line.text, line.x(), line.y(), font.size(), getTextColor());
    }

    public class WrappedLine {
        private final int index;
        public final String text;
        public final float width;

        WrappedLine(int index, String text, float width) {
            this.index = index;
            this.text = text;
            this.width = width;
        }

        public float x() {
            return contentBox.x + alignX();
        }

        public float y() {
            return contentBox.y + font.getHeight() * index;
        }

        public float alignX() {
            switch (align) {
                case LEFT:
                    return 0;
                case CENTER:
                    return (contentBox.width - width) / 2f;
                case RIGHT:
                    return contentBox.width - width;
                default:
                    throw new IllegalStateException("Unexpected value: " + align);
            }
        }
    }

    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum WrapMode {
        NONE,
        WORD
    }
}
