package net.glintwein.ui.data;

public class BorderRadius {
    public static final BorderRadius ZERO = new BorderRadius(0);

    public float topLeft;
    public float topRight;
    public float bottomRight;
    public float bottomLeft;

    public BorderRadius() {
    }

    public BorderRadius(float radius) {
        this.topLeft = radius;
        this.topRight = radius;
        this.bottomRight = radius;
        this.bottomLeft = radius;
    }

    public BorderRadius top(float radius) {
        this.topLeft = radius;
        this.topRight = radius;
        return this;
    }

    public BorderRadius bottom(float radius) {
        this.bottomLeft = radius;
        this.bottomRight = radius;
        return this;
    }

    public BorderRadius left(float radius) {
        this.topLeft = radius;
        this.bottomLeft = radius;
        return this;
    }

    public BorderRadius right(float radius) {
        this.topRight = radius;
        this.bottomRight = radius;
        return this;
    }

    public BorderRadius topLeft(float radius) {
        this.topLeft = radius;
        return this;
    }

    public BorderRadius topRight(float radius) {
        this.topRight = radius;
        return this;
    }

    public BorderRadius bottomRight(float radius) {
        this.bottomRight = radius;
        return this;
    }

    public BorderRadius bottomLeft(float radius) {
        this.bottomLeft = radius;
        return this;
    }

    public static BorderRadius of(float radius) {
        if (radius == 0) return ZERO;
        return new BorderRadius(radius);
    }
}
