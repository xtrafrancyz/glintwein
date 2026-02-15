package net.glintwein.ui.data;

public class Box {
    public float x;
    public float y;
    public float width;
    public float height;

    public Box expand(float val) {
        x -= val;
        y -= val;
        width += val * 2;
        height += val * 2;
        return this;
    }

    public void set(Box other) {
        this.x = other.x;
        this.y = other.y;
        this.width = other.width;
        this.height = other.height;
    }

    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    public Box copy() {
        Box copy = new Box();
        copy.set(this);
        return copy;
    }
}
