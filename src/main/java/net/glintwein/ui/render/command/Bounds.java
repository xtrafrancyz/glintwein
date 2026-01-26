package net.glintwein.ui.render.command;

public class Bounds {
    public float minX;
    public float minY;
    public float maxX;
    public float maxY;

    private Bounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public boolean intersects(Bounds other) {
        return this.minX < other.maxX && this.maxX > other.minX &&
            this.minY < other.maxY && this.maxY > other.minY;
    }

    public static Bounds fromXYWH(float x, float y, float width, float height) {
        return new Bounds(x, y, x + width, y + height);
    }

    public static Bounds fromMinMax(float minX, float minY, float maxX, float maxY) {
        return new Bounds(minX, minY, maxX, maxY);
    }
}
