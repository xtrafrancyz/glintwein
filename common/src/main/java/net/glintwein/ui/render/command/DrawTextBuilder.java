package net.glintwein.ui.render.command;

import net.glintwein.ui.data.Gradient;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.font.SizedFont;

public class DrawTextBuilder {
    GigaFont font;
    String text;
    float x;
    float y;
    float size;
    int color;
    Gradient gradient;
    int outlineColor;
    float outlineWidth;

    private DrawTextBuilder() {
        this.color = 0xFFFFFFFF;
    }

    public DrawTextBuilder text(String text) {
        this.text = text;
        return this;
    }

    public DrawTextBuilder font(GigaFont font) {
        this.font = font;
        return this;
    }

    public DrawTextBuilder font(SizedFont font) {
        return font(font.font(), font.size());
    }

    public DrawTextBuilder font(GigaFont font, float size) {
        this.font = font;
        this.size = size;
        return this;
    }

    public DrawTextBuilder size(float size) {
        this.size = size;
        return this;
    }

    public DrawTextBuilder offset(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public DrawTextBuilder color(int color) {
        this.gradient = null;
        this.color = color;
        return this;
    }

    public DrawTextBuilder color(Gradient gradient) {
        this.color = 0xFFFFFFFF;
        this.gradient = gradient;
        return this;
    }

    public DrawTextBuilder outline(int color, float width) {
        this.outlineColor = color;
        this.outlineWidth = width;
        return this;
    }

    public static DrawTextBuilder of(String text, GigaFont font, float size) {
        return new DrawTextBuilder().text(text).font(font, size);
    }

    public static DrawTextBuilder of(String text, SizedFont font) {
        return of(text, font.font(), font.size());
    }

    public static DrawTextBuilder of() {
        return new DrawTextBuilder();
    }
}
