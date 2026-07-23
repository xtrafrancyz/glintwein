package net.glintwein.ui.data;

import net.glintwein.ui.util.ARGB;

public class Gradient {
    private final int tl, tr, br, bl;

    private Gradient(int tl, int tr, int br, int bl) {
        this.tl = tl;
        this.tr = tr;
        this.br = br;
        this.bl = bl;
    }

    public Gradient mulAlpha(float alpha) {
        return new Gradient(
            ARGB.mulAlpha(tl, alpha),
            ARGB.mulAlpha(tr, alpha),
            ARGB.mulAlpha(br, alpha),
            ARGB.mulAlpha(bl, alpha)
        );
    }

    public static Gradient uniform(int color) {
        return new Gradient(color, color, color, color);
    }

    public static Gradient leftToRight(int left, int right) {
        return new Gradient(left, right, right, left);
    }

    public static Gradient rightToLeft(int right, int left) {
        return new Gradient(left, right, right, left);
    }

    public static Gradient topToBottom(int top, int bottom) {
        return new Gradient(top, top, bottom, bottom);
    }

    public static Gradient bottomToTop(int bottom, int top) {
        return new Gradient(top, top, bottom, bottom);
    }

    public static Gradient fromCorners(int tl, int tr, int br, int bl) {
        return new Gradient(tl, tr, br, bl);
    }

    public int topLeft() {
        return tl;
    }

    public int topRight() {
        return tr;
    }

    public int bottomRight() {
        return br;
    }

    public int bottomLeft() {
        return bl;
    }

    public boolean isFullyTransparent() {
        return ARGB.alpha(tl) + ARGB.alpha(tr) + ARGB.alpha(br) + ARGB.alpha(bl) == 0;
    }
}
