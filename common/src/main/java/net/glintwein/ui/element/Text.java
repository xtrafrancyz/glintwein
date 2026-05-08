package net.glintwein.ui.element;

import net.glintwein.platform.YogaMeasureFunction;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.Size;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.font.SizedFont;

import java.util.ArrayList;
import java.util.List;

public class Text extends LeafElement {
    private String text = "";
    protected SizedFont font;
    protected int color = 0xFFFFFFFF;
    private WrapMode wrapMode = WrapMode.WORD;
    private Align align = Align.LEFT;

    private List<WrappedLine> splitLinesText;
    private float splitLinesMaxWidth = 0;
    private final List<WrappedLine> wrappedLines = new ArrayList<>();
    private final List<RenderLine> renderLines = new ArrayList<>();

    public Text() {
        this("");
    }

    public Text(String text) {
        this.font = GlobalUIState.getDefaultTextFont();
        setMeasureFunction(this::wrapAndMeasure);
        setText(text);
    }

    private Size wrapAndMeasure(float width, YogaMeasureFunction.SizeMode widthMode,
                                float height, YogaMeasureFunction.SizeMode heightMode) {
        // wrap content
        wrappedLines.clear();
        renderLines.clear();
        float contentWidth = 0;
        if (widthMode != YogaMeasureFunction.SizeMode.UNDEFINED && wrapMode == WrapMode.WORD) {
            List<String> wrapped = new ArrayList<>();
            font.wrapText(this.text, width, wrapped);
            for (String line : wrapped) {
                float lineWidth = font.getWidth(line);
                wrappedLines.add(new WrappedLine(line, lineWidth));
                contentWidth = Math.max(contentWidth, lineWidth);
            }
        } else {
            if (splitLinesText == null) {
                String[] lines = this.text.split("\n", -1);
                splitLinesMaxWidth = 0;
                splitLinesText = new ArrayList<>(lines.length);
                for (String line : lines) {
                    float lineWidth = font.getWidth(line);
                    splitLinesText.add(new WrappedLine(line, lineWidth));
                    splitLinesMaxWidth = Math.max(splitLinesMaxWidth, lineWidth);
                }
            }
            wrappedLines.addAll(splitLinesText);
            contentWidth = splitLinesMaxWidth;
        }
        if (wrappedLines.isEmpty())
            wrappedLines.add(new WrappedLine("", 0));
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
        if (this.align == align)
            return;
        this.align = align;
        markDirty();
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

    protected List<RenderLine> getRenderLines() {
        if (renderLines.isEmpty()) {
            if (wrappedLines.isEmpty()) {
                wrapAndMeasure(
                    contentBox.width, YogaMeasureFunction.SizeMode.EXACTLY,
                    contentBox.height, YogaMeasureFunction.SizeMode.EXACTLY
                );
            }

            float y = contentBox.y;
            for (WrappedLine line : wrappedLines) {
                float x;
                switch (align) {
                    case LEFT:
                        x = contentBox.x;
                        break;
                    case CENTER:
                        x = contentBox.x + (contentBox.width - line.width) / 2f;
                        break;
                    case RIGHT:
                        x = contentBox.x + contentBox.width - line.width;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + align);
                }
                renderLines.add(new RenderLine(line.text, line.width, x, y));
                y += font.getHeight();
            }
            if (renderLines.isEmpty()) {
                // ensure phantom empty string, not to crash
                renderLines.add(new RenderLine("", 0, 0, 0));
            }
        }
        return renderLines;
    }

    @Override
    protected void readYogaLayout() {
        super.readYogaLayout();
        renderLines.clear();
    }

    @Override
    protected void markDirty() {
        super.markDirty();
        renderLines.clear();
        wrappedLines.clear();
        splitLinesText = null;
    }

    @Override
    protected void drawContent(Context ctx) {
        for (RenderLine line : getRenderLines())
            drawLine(ctx, line);
    }

    protected void drawLine(Context ctx, RenderLine line) {
        ctx.drawText(font.font(), line.text, line.x, line.y, font.size(), getTextColor());
    }

    private static class WrappedLine {
        String text;
        float width;

        WrappedLine(String text, float width) {
            this.text = text;
            this.width = width;
        }
    }

    public static class RenderLine {
        public String text;
        public float width;
        public float x;
        public float y;

        RenderLine(String text, float width, float x, float y) {
            this.text = text;
            this.width = width;
            this.x = x;
            this.y = y;
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
