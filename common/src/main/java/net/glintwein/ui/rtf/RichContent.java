package net.glintwein.ui.rtf;

import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.font.SizedFont;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class RichContent {
    public List<Span> spans;

    public RichContent(List<Span> spans) {
        this.spans = spans;
    }

    public RichContent(Span... spans) {
        this.spans = Arrays.asList(spans);
    }

    public void append(Span span) {
        spans.add(span);
    }

    public void append(RichContent content) {
        spans.addAll(content.spans);
    }

    public void filterTextSpans(BiConsumer<TextSpan, List<Span>> filter) {
        List<Span> newSpans = new ArrayList<>(spans.size());
        for (Span span : spans) {
            if (span instanceof TextSpan)
                filter.accept((TextSpan) span, newSpans);
            else
                newSpans.add(span);
        }
        spans = newSpans;
    }

    /**
     * <pre>{@code
     * RichContent content = ...;
     * content.filterSpans((span, output) -> {
     *    if (span instanceof TextSpan textSpan) {
     *       // Modify the textSpan or filter it out
     *       if (textSpan.getText().contains("skip")) {
     *          // Skip this span (filter it out)
     *          return;
     *       }
     *       // Modify the textSpan (e.g., change its color)
     *       TextSpan.Style newStyle = textSpan.getStyle().copy();
     *       newStyle.color = 0xFF0000; // Change color to red
     *       TextSpan modifiedSpan = new TextSpan(textSpan.text, newStyle);
     *       output.add(modifiedSpan);
     *    } else {
     *       // Keep other types of spans unchanged
     *       output.add(span);
     *    }
     * });
     * }</pre>
     */
    public void filterSpans(BiConsumer<Span, List<Span>> filter) {
        List<Span> newSpans = new ArrayList<>(spans.size());
        for (Span span : spans)
            filter.accept(span, newSpans);
        spans = newSpans;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Span> spans = new ArrayList<>();
        private TextSpan.Style style = new TextSpan.Style();
        private TextSpan.Style bakedStyle = null;

        public Builder font(GigaFont font) {
            if (font == null)
                throw new IllegalArgumentException("font cannot be null");
            if (font == style.font)
                return this;
            bakedStyle = null;
            style.font = font;
            return this;
        }

        public Builder font(GigaFont font, float size) {
            if (font == null)
                throw new IllegalArgumentException("font cannot be null");
            if (font == style.font && size == style.fontSize)
                return this;
            bakedStyle = null;
            style.font = font;
            style.fontSize = size;
            return this;
        }

        public Builder font(SizedFont font) {
            if (font == null || font.font() == null)
                throw new IllegalArgumentException("font cannot be null");
            if (font.font() == style.font && font.size() == style.fontSize)
                return this;
            bakedStyle = null;
            style.font = font.font();
            style.fontSize = font.size();
            return this;
        }

        public Builder fontSize(float fontSize) {
            if (fontSize == style.fontSize)
                return this;
            bakedStyle = null;
            style.fontSize = fontSize;
            return this;
        }

        public Builder color(int color) {
            if (color == style.color)
                return this;
            bakedStyle = null;
            style.color = color;
            return this;
        }

        public Builder outline(int color, float width) {
            if (color == style.outlineColor && width == style.outlineWidth)
                return this;
            bakedStyle = null;
            style.outlineColor = color;
            style.outlineWidth = width;
            return this;
        }

        public Builder resetOutline() {
            return outline(0, 0);
        }

        public Builder resetStyle() {
            style = new TextSpan.Style();
            return this;
        }

        public Builder append(String text) {
            if (bakedStyle == null)
                bakedStyle = style.copy();
            spans.add(new TextSpan(text, bakedStyle));
            return this;
        }

        public Builder append(Span span) {
            spans.add(span);
            return this;
        }

        public Builder append(RichContent content) {
            spans.addAll(content.spans);
            return this;
        }

        public RichContent build() {
            return new RichContent(spans);
        }
    }
}
