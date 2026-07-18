package net.glintwein.ui.element.component;

import net.glintwein.platform.YogaMeasureFunction;
import net.glintwein.ui.data.Size;
import net.glintwein.ui.element.LeafElement;
import net.glintwein.ui.element.Text;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.rtf.*;

import java.util.ArrayList;
import java.util.List;

public class RichElement extends LeafElement {
    private Text.WrapMode wrapMode = Text.WrapMode.WORD;
    private Text.Align align = Text.Align.LEFT;
    private final List<RenderToken> renderTokens = new ArrayList<>();
    private final List<Line> lines = new ArrayList<>();

    public RichElement() {
        setMeasureFunction(this::wrapAndMeasure);
    }

    public RichElement(RichContent content) {
        this();
        setContent(content);
    }

    private Size wrapAndMeasure(float width, YogaMeasureFunction.SizeMode widthMode,
                                float height, YogaMeasureFunction.SizeMode heightMode) {
        // wrap content
        lines.clear();

        float maxWidth;
        if (widthMode != YogaMeasureFunction.SizeMode.UNDEFINED && wrapMode == Text.WrapMode.WORD)
            maxWidth = width;
        else
            maxWidth = Float.MAX_VALUE;

        float contentWidth = 0;
        float contentHeight = 0;

        Line line = new Line();
        for (RenderToken token : renderTokens) {
            if (token instanceof RenderTokenNewline) {
                lines.add(line);
                line = new Line();
                line.height = token.getHeight();
                continue;
            }
            float tokenWidth = token.getWidth();
            if (line.width + tokenWidth > maxWidth) {
                lines.add(line);
                line = new Line();
                if (token instanceof RenderTokenSpace) {
                    line.height = token.getHeight();
                    continue;  // skip leading space
                }
            }

            // merge with previous token if possible
            if (!line.tokens.isEmpty()) {
                RenderToken prev = line.tokens.get(line.tokens.size() - 1);
                RenderToken merged = prev.tryMergeNext(token);
                if (merged != null) {
                    line.width = line.width - prev.getWidth() + merged.getWidth();
                    line.tokens.set(line.tokens.size() - 1, merged);
                    continue;
                }
            }

            line.tokens.add(token);
            line.width += tokenWidth;
        }
        if (!line.tokens.isEmpty())
            lines.add(line);

        for (Line line0 : lines) {
            for (RenderToken token : line0.tokens)
                line0.height = Math.max(line0.height, token.getHeight());
            contentWidth = Math.max(contentWidth, line0.width);
            contentHeight += line0.height;
        }

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

    @Override
    protected void readYogaLayout() {
        super.readYogaLayout();
        if (lines.isEmpty()) {
            wrapAndMeasure(
                contentBox.width, YogaMeasureFunction.SizeMode.EXACTLY,
                contentBox.height, YogaMeasureFunction.SizeMode.EXACTLY
            );
        }
    }

    public void setAlign(Text.Align align) {
        this.align = align;
    }

    public void setContent(RichContent content) {
        renderTokens.clear();
        for (Span span : content.spans)
            span.generateRenderTokens(renderTokens);
        markDirty();
    }

    @Override
    protected void markDirty() {
        super.markDirty();
        lines.clear();
    }

    @Override
    protected void drawContent(Context ctx) {
        float y = contentBox.y;
        for (Line line : lines) {
            float x = contentBox.x;
            if (align == Text.Align.CENTER)
                x += (contentBox.width - line.width) / 2;
            else if (align == Text.Align.RIGHT)
                x += contentBox.width - line.width;

            for (RenderToken token : line.tokens) {
                token.draw(ctx, x, y + line.height - token.getHeight());
                x += token.getWidth();
            }
            y += line.height;
        }
    }

    private static class Line {
        final List<RenderToken> tokens = new ArrayList<>();
        float width = 0;
        float height = 0;
    }
}
