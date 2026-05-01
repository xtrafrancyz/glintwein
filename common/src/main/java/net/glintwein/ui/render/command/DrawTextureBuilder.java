package net.glintwein.ui.render.command;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Box;
import net.glintwein.ui.render.texture.Sprite;

public class DrawTextureBuilder {
    float u0, v0, u1 = 1, v1 = 1;
    int texture;
    float x0, y0, x1, y1;
    BorderRadius radius;
    int color;
    int outlineColor;
    float outlineWidth;

    private DrawTextureBuilder(float x0, float y0, float x1, float y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.radius = BorderRadius.ZERO;
        this.color = 0xffffffff;
        this.outlineColor = 0x00000000;
        this.outlineWidth = 0;
    }

    public DrawTextureBuilder uv(float u0, float v0, float u1, float v1) {
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        return this;
    }

    public DrawTextureBuilder texture(int texture) {
        this.texture = texture;
        return this;
    }

    public DrawTextureBuilder texture(Sprite sprite) {
        this.texture = sprite.textureId;
        this.u0 = sprite.u0;
        this.v0 = sprite.v0;
        this.u1 = sprite.u1;
        this.v1 = sprite.v1;
        return this;
    }

    public DrawTextureBuilder radius(BorderRadius radius) {
        this.radius = radius;
        return this;
    }

    public DrawTextureBuilder radius(float radius) {
        this.radius = BorderRadius.of(radius);
        return this;
    }

    public DrawTextureBuilder color(int color) {
        this.color = color;
        return this;
    }

    public DrawTextureBuilder outline(int color, float width) {
        this.outlineColor = color;
        this.outlineWidth = width;
        return this;
    }

    public DrawTextureBuilder expandByOutline() {
        this.x0 -= outlineWidth;
        this.y0 -= outlineWidth;
        this.x1 += outlineWidth;
        this.y1 += outlineWidth;
        return this;
    }

    public static DrawTextureBuilder fromXYWH(float x, float y, float width, float height) {
        return new DrawTextureBuilder(x, y, x + width, y + height);
    }

    public static DrawTextureBuilder fromMinMax(float x0, float y0, float x1, float y1) {
        return new DrawTextureBuilder(x0, y0, x1, y1);
    }

    public static DrawTextureBuilder fromBox(Box box) {
        return fromXYWH(box.x, box.y, box.width, box.height);
    }
}
