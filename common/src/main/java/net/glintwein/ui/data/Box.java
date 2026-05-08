package net.glintwein.ui.data;

import net.glintwein.ui.GlobalUIState;

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

    public void setXY(Box other) {
        this.x = other.x;
        this.y = other.y;
    }

    public void setSize(Box other) {
        this.width = other.width;
        this.height = other.height;
    }

    public void set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void snapToPixels() {
        float x = this.x;
        float y = this.y;
        float right = GlobalUIState.snapToPixel(x + width);
        float bottom = GlobalUIState.snapToPixel(y + height);
        this.x = GlobalUIState.snapToPixel(x);
        this.y = GlobalUIState.snapToPixel(y);
        this.width = right - this.x;
        this.height = bottom - this.y;
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
