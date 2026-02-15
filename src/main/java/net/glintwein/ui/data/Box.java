package net.glintwein.ui.data;

public class Box {
    public float x;
    public float y;
    public float width;
    public float height;

    public void set(Box other) {
        this.x = other.x;
        this.y = other.y;
        this.width = other.width;
        this.height = other.height;
    }

    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
