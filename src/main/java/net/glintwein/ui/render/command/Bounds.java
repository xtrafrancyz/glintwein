package net.glintwein.ui.render.command;

import net.glintwein.ui.data.Box;
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

    public Bounds intersection(Bounds other) {
        float newMinX = Math.max(this.minX, other.minX);
        float newMinY = Math.max(this.minY, other.minY);
        float newMaxX = Math.min(this.maxX, other.maxX);
        float newMaxY = Math.min(this.maxY, other.maxY);
        if (newMinX >= newMaxX || newMinY >= newMaxY)
            return null;
        return new Bounds(newMinX, newMinY, newMaxX, newMaxY);
    }

    public Bounds transformAxisAligned(Matrix3x2fc matrix) {
        Vector2f v0 = matrix.transformPosition(this.minX, this.minY, TEMP_VECTORS[0]);
        Vector2f v1 = matrix.transformPosition(this.maxX, this.maxY, TEMP_VECTORS[1]);
        this.minX = Math.min(v0.x, v1.x);
        this.minY = Math.min(v0.y, v1.y);
        this.maxX = Math.max(v0.x, v1.x);
        this.maxY = Math.max(v0.y, v1.y);
        return this;
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

    public static Bounds fromBox(Box box) {
        return fromXYWH(box.x, box.y, box.width, box.height);
    }

    public static Bounds fromXYWH(float x, float y, float width, float height) {
        return new Bounds(x, y, x + width, y + height);
    }

    public static Bounds fromMinMax(float minX, float minY, float maxX, float maxY) {
        return new Bounds(minX, minY, maxX, maxY);
    }
}
