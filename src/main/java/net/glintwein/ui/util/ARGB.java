package net.glintwein.ui.util;

import net.minecraft.util.Mth;

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

    public static int mulAlpha(int color, float alpha) {
        return setAlpha(color, alphaF(color) * alpha);
    }

    public static int setAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int setAlpha(int color, float alpha) {
        return setAlpha(color, (int) (alpha * 255));
    }

    public static int ofRGB(int r, int g, int b) {
        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int ofARGB(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    /**
     * Конвертирует цвет из HSLA в ARGB формат (int). Все значения от 0.0 до 1.0
     */
    public static int ofHSLA(float hue, float saturation, float lightness, float alpha) {
        float r, g, b;

        if (saturation == 0.0f) {
            r = g = b = lightness; // achromatic
        } else {
            float q = lightness < 0.5f
                ? lightness * (1.0f + saturation)
                : lightness + saturation - lightness * saturation;
            float p = 2.0f * lightness - q;
            r = hue2rgb(p, q, hue + 1.0f / 3.0f);
            g = hue2rgb(p, q, hue);
            b = hue2rgb(p, q, hue - 1.0f / 3.0f);
        }

        return ofARGB(
            Math.min(255, Math.max(0, (int) (alpha * 255.0f))),
            Math.min(255, Math.max(0, (int) (r * 255.0f))),
            Math.min(255, Math.max(0, (int) (g * 255.0f))),
            Math.min(255, Math.max(0, (int) (b * 255.0f)))
        );
    }

    private static float hue2rgb(float p, float q, float t) {
        if (t < 0.0f) t += 1.0f;
        if (t > 1.0f) t -= 1.0f;
        if (t < 1.0f / 6.0f) return p + (q - p) * 6.0f * t;
        if (t < 1.0f / 2.0f) return q;
        if (t < 2.0f / 3.0f) return p + (q - p) * (2.0f / 3.0f - t) * 6.0f;
        return p;
    }

    public static int lerp(float t, int a, int b) {
        t = Mth.clamp(t, 0, 1);
        return (int) ((a >> 24 & 255) * (1.0f - t) + ((b >> 24 & 255) * t)) << 24 |  // a
            (int) ((a >> 16 & 255) * (1.0f - t) + ((b >> 16 & 255) * t)) << 16 | // r
            (int) ((a >> 8 & 255) * (1.0f - t) + ((b >> 8 & 255) * t)) << 8 |    // g
            (int) ((a & 255) * (1.0f - t) + ((b & 255) * t));                    // b
    }
}
