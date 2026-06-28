package net.glintwein.ui.render.command;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Box;
import net.glintwein.ui.data.Gradient;

public class DrawRectBuilder {
    float x0, y0, x1, y1;
    BorderRadius radius;
    int color;
    Gradient gradient;
    int outlineColor;
    float outlineWidth;

    private DrawRectBuilder(float x0, float y0, float x1, float y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.radius = BorderRadius.ZERO;
        this.color = 0x00000000;
        this.outlineColor = 0x00000000;
        this.outlineWidth = 0;
    }

    public DrawRectBuilder radius(BorderRadius radius) {
        this.radius = radius;
        return this;
    }

    public DrawRectBuilder radius(float radius) {
        this.radius = BorderRadius.of(radius);
        return this;
    }

    public DrawRectBuilder color(int color) {
        this.gradient = null;
        this.color = color;
        return this;
    }

    public DrawRectBuilder color(Gradient gradient) {
        this.color = 0xFFFFFFFF;
        this.gradient = gradient;
        return this;
    }

    public DrawRectBuilder outline(int color, float width) {
        this.outlineColor = color;
        this.outlineWidth = width;
        return this;
    }

    public DrawRectBuilder offset(float dx, float dy) {
        this.x0 += dx;
        this.y0 += dy;
        this.x1 += dx;
        this.y1 += dy;
        return this;
    }

    public DrawRectBuilder expand(float amount) {
        this.x0 -= amount;
        this.y0 -= amount;
        this.x1 += amount;
        this.y1 += amount;
        return this;
    }

    public DrawRectBuilder expandByOutline() {
        this.x0 -= outlineWidth;
        this.y0 -= outlineWidth;
        this.x1 += outlineWidth;
        this.y1 += outlineWidth;
        return this;
    }

    public static DrawRectBuilder fromXYWH(float x, float y, float width, float height) {
        return new DrawRectBuilder(x, y, x + width, y + height);
    }

    public static DrawRectBuilder fromMinMax(float x0, float y0, float x1, float y1) {
        return new DrawRectBuilder(x0, y0, x1, y1);
    }

    public static DrawRectBuilder fromBox(Box box) {
        return fromXYWH(box.x, box.y, box.width, box.height);
    }
}
