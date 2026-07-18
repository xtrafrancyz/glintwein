package net.glintwein.ui.rtf;

import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.render.font.GigaFont;

import java.util.List;

public class TextSpan implements Span {
    public String text;
    public Style style;

    public TextSpan(String text, Style style) {
        this.text = text;
        this.style = style;
    }

    @Override
    public void generateRenderTokens(List<RenderToken> output) {
        String[] lines = text.split("\n", -1);
        RenderTokenSpace space = null;
        for (int i = 0; i < lines.length; i++) {
            if (i != 0)
                output.add(RenderTokenNewline.get(style.font.getHeight(style.fontSize)));
            String[] words = lines[i].split(" ", -1);
            for (int j = 0; j < words.length; j++) {
                if (j != 0) {
                    if (space == null)
                        space = RenderTokenSpace.get(style.font.getSpaceWidth(style.fontSize));
                    output.add(space);
                }
                if (words[j].isEmpty())
                    continue;
                output.add(new RenderTokenText(words[j], style));
            }
        }
    }

    public static class Style {
        public GigaFont font = GlobalUIState.getDefaultFont();
        public float fontSize = GlobalUIState.getDefaultTextFont().size();
        public int color = 0xFFFFFFFF;
        public int outlineColor;
        public float outlineWidth;

        public Style copy() {
            Style newStyle = new Style();
            newStyle.font = this.font;
            newStyle.fontSize = this.fontSize;
            newStyle.color = this.color;
            newStyle.outlineColor = this.outlineColor;
            newStyle.outlineWidth = this.outlineWidth;
            return newStyle;
        }
    }
}
