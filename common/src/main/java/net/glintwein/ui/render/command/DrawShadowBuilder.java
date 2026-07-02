package net.glintwein.ui.render.command;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Gradient;

public class DrawShadowBuilder {
    float x0, y0, x1, y1;
    BorderRadius radius;
    int colorTL, colorTR, colorBR, colorBL;
    float blurSpread;

    private DrawShadowBuilder(float x0, float y0, float x1, float y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.radius = BorderRadius.ZERO;
        this.colorTL = 0x00000000;
        this.colorTR = 0x00000000;
        this.colorBR = 0x00000000;
        this.colorBL = 0x00000000;
        this.blurSpread = 10.0f;
    }

    public DrawShadowBuilder radius(BorderRadius radius) {
        if (radius == null)
            throw new IllegalArgumentException("radius cannot be null");
        this.radius = radius;
        return this;
    }

    public DrawShadowBuilder radius(float radius) {
        this.radius = BorderRadius.of(radius);
        return this;
    }

    public DrawShadowBuilder color(int color) {
        this.colorTL = color;
        this.colorTR = color;
        this.colorBR = color;
        this.colorBL = color;
        return this;
    }

    public DrawShadowBuilder color(int colorTL, int colorTR, int colorBR, int colorBL) {
        this.colorTL = colorTL;
        this.colorTR = colorTR;
        this.colorBR = colorBR;
        this.colorBL = colorBL;
        return this;
    }

    public DrawShadowBuilder color(Gradient gradient) {
        this.colorTL = gradient.topLeft();
        this.colorTR = gradient.topRight();
        this.colorBR = gradient.bottomRight();
        this.colorBL = gradient.bottomLeft();
        return this;
    }

    public DrawShadowBuilder blurRadius(float radius) {
        this.blurSpread = radius;
        return this;
    }

    public DrawShadowBuilder offset(float dx, float dy) {
        this.x0 += dx;
        this.y0 += dy;
        this.x1 += dx;
        this.y1 += dy;
        return this;
    }

    public DrawShadowBuilder expand(float amount) {
        this.x0 -= amount;
        this.y0 -= amount;
        this.x1 += amount;
        this.y1 += amount;
        return this;
    }

    public static DrawShadowBuilder fromXYWH(float x, float y, float width, float height) {
        return new DrawShadowBuilder(x, y, x + width, y + height);
    }
}
