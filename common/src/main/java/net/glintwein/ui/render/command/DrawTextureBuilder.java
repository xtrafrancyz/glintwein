package net.glintwein.ui.render.command;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Box;
import net.glintwein.ui.data.NineSliceSpec;
import net.glintwein.ui.render.texture.Sprite;

public class DrawTextureBuilder {
    float u0, v0, u1 = 1, v1 = 1;
    int texture;
    float x0, y0, x1, y1;
    BorderRadius radius;
    int color;
    int outlineColor;
    float outlineWidth;
    NineSliceSpec nineSlice;

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

    public DrawTextureBuilder uv(int x, int y, int width, int height, int textureWidth, int textureHeight) {
        this.u0 = (float) x / textureWidth;
        this.v0 = (float) y / textureHeight;
        this.u1 = (float) (x + width) / textureWidth;
        this.v1 = (float) (y + height) / textureHeight;
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
        if (radius == null)
            throw new IllegalArgumentException("radius cannot be null");
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

    public DrawTextureBuilder offset(float dx, float dy) {
        this.x0 += dx;
        this.y0 += dy;
        this.x1 += dx;
        this.y1 += dy;
        return this;
    }

    public DrawTextureBuilder expand(float amount) {
        this.x0 -= amount;
        this.y0 -= amount;
        this.x1 += amount;
        this.y1 += amount;
        return this;
    }

    public DrawTextureBuilder expandByOutline() {
        this.x0 -= outlineWidth;
        this.y0 -= outlineWidth;
        this.x1 += outlineWidth;
        this.y1 += outlineWidth;
        return this;
    }

    public DrawTextureBuilder nineSlice(NineSliceSpec spec) {
        this.nineSlice = spec;
        return this;
    }

    public DrawTextureBuilder nineSliceType(NineSliceSpec.Type type) {
        if (this.nineSlice == null)
            this.nineSlice = new NineSliceSpec(type, 0, 0, 0, 0, 1, 1);
        else
            this.nineSlice = this.nineSlice.withType(type);
        return this;
    }

    public DrawTextureBuilder nineSliceScale(float scale) {
        if (this.nineSlice == null)
            this.nineSlice = new NineSliceSpec(NineSliceSpec.Type.STRETCH, 0, 0, 0, 0, 1, 1, scale);
        else
            this.nineSlice = this.nineSlice.withScale(scale);
        return this;
    }

    public DrawTextureBuilder nineSliceInsets(int left, int top, int right, int bottom, int textureWidth, int textureHeight) {
        if (this.nineSlice == null)
            this.nineSlice = new NineSliceSpec(NineSliceSpec.Type.STRETCH, left, top, right, bottom, textureWidth, textureHeight);
        else
            this.nineSlice = this.nineSlice.withInsets(left, top, right, bottom, textureWidth, textureHeight);
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
