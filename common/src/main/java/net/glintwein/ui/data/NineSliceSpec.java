package net.glintwein.ui.data;

public class NineSliceSpec {
    private final Type type;
    private final float scale;
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;
    private final int textureWidth;
    private final int textureHeight;

    public NineSliceSpec(Type type, int left, int top, int right, int bottom, int textureWidth, int textureHeight) {
        this(type, left, top, right, bottom, textureWidth, textureHeight, 1.0f);
    }

    public NineSliceSpec(Type type, int left, int top, int right, int bottom, int textureWidth, int textureHeight, float scale) {
        this.type = type;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.scale = scale;
    }

    public Type type() {
        return type;
    }

    public float scale() {
        return scale;
    }

    public int left() {
        return left;
    }

    public int top() {
        return top;
    }

    public int right() {
        return right;
    }

    public int bottom() {
        return bottom;
    }

    public int textureWidth() {
        return textureWidth;
    }

    public int textureHeight() {
        return textureHeight;
    }

    public NineSliceSpec withScale(float scale) {
        return new NineSliceSpec(type, left, top, right, bottom, textureWidth, textureHeight, scale);
    }

    public NineSliceSpec withType(Type type) {
        return new NineSliceSpec(type, left, top, right, bottom, textureWidth, textureHeight, scale);
    }

    public NineSliceSpec withInsets(int left, int top, int right, int bottom, int textureWidth, int textureHeight) {
        return new NineSliceSpec(type, left, top, right, bottom, textureWidth, textureHeight, scale);
    }

    public enum Type {
        /**
         * Stretch the center region to fill the available space, while keeping the corners and edges at their original size.
         */
        STRETCH,
        /**
         * Repeat the center region to fill the available space, while keeping the corners and edges at their original size.
         */
        TILE
    }
}
