package net.glintwein.ui.render.font;

import java.util.List;

public class SizedFont {
    private final GigaFont font;
    private final float size;

    public SizedFont(GigaFont font, float size) {
        this.font = font;
        this.size = size;
    }

    public float getWidth(String text) {
        return font.getWidth(text, size);
    }

    public float getHeight() {
        return font.getHeight(size);
    }

    public void wrapText(String text, float maxWidth, List<String> wrapped) {
        font.wrapText(text, size, maxWidth, wrapped);
    }

    public String trimToWidth(String text, float maxWidth) {
        return font.trimToWidth(text, size, maxWidth);
    }

    public GigaFont font() {
        return font;
    }

    public float size() {
        return size;
    }

    public SizedFont withSize(float newSize) {
        return new SizedFont(font, newSize);
    }
}
