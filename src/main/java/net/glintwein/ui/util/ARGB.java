package net.glintwein.ui.util;

public class ARGB {
    public static int alpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static float alphaF(int color) {
        return alpha(color) / 255.0f;
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int setAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int setAlpha(int color, float alpha) {
        return setAlpha(color, (int) (alpha * 255));
    }
}
