package net.glintwein.ui.render.command;

import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

public class Bounds {
    private static final Vector2f[] TEMP_VECTORS = {
        new Vector2f(),
        new Vector2f(),
        new Vector2f(),
        new Vector2f()
    };

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

    public Bounds transformMaxBounds(Matrix3x2fc matrix) {
        Vector2f v0 = matrix.transformPosition(this.minX, this.minY, TEMP_VECTORS[0]);
        Vector2f v1 = matrix.transformPosition(this.maxX, this.minY, TEMP_VECTORS[1]);
        Vector2f v2 = matrix.transformPosition(this.maxX, this.maxY, TEMP_VECTORS[2]);
        Vector2f v3 = matrix.transformPosition(this.minX, this.maxY, TEMP_VECTORS[3]);
        this.minX = Math.min(Math.min(v0.x, v1.x), Math.min(v2.x, v3.x));
        this.minY = Math.min(Math.min(v0.y, v1.y), Math.min(v2.y, v3.y));
        this.maxX = Math.max(Math.max(v0.x, v1.x), Math.max(v2.x, v3.x));
        this.maxY = Math.max(Math.max(v0.y, v1.y), Math.max(v2.y, v3.y));
        return this;
    }

    public static Bounds fromXYWH(float x, float y, float width, float height) {
        return new Bounds(x, y, x + width, y + height);
    }

    public static Bounds fromMinMax(float minX, float minY, float maxX, float maxY) {
        return new Bounds(minX, minY, maxX, maxY);
    }
}
