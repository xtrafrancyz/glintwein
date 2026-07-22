package net.glintwein.ui.util;

import org.joml.Matrix3x2f;

public class GMath {
    public static final float PI = (float) Math.PI;
    public static final float HALF_PI = (float) (Math.PI / 2.0);
    public static final float TWO_PI = (float) (Math.PI * 2.0);
    public static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
    public static final float RAD_TO_DEG = 180.0F / (float) Math.PI;

    public static float square(float value) {
        return value * value;
    }

    public static float sin(float value) {
        return (float) Math.sin(value);
    }

    public static float cos(float value) {
        return (float) Math.cos(value);
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static float abs(float value) {
        return Math.abs(value);
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static int floor(float value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static int ceil(float value) {
        int i = (int) value;
        return value > i ? i + 1 : i;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float roundX(Matrix3x2f m, float value) {
        if (m.m01 != 0.0f)
            return value;
        float scale = m.m00;
        float subpixel = m.m20 - Math.round(m.m20);
        return (Math.round(value * scale) - subpixel) / scale;
    }

    public static float roundY(Matrix3x2f m, float value) {
        if (m.m10 != 0.0f)
            return value;
        float scale = m.m11;
        float subpixel = m.m21 - Math.round(m.m21);
        return (Math.round(value * scale) - subpixel) / scale;
    }

    public static float floorX(Matrix3x2f m, float value) {
        if (m.m01 != 0.0f)
            return value;
        float scale = m.m00;
        float subpixel = m.m20 - Math.round(m.m20);
        return ((float) Math.floor(value * scale) - subpixel) / scale;
    }

    public static float floorY(Matrix3x2f m, float value) {
        if (m.m10 != 0.0f)
            return value;
        float scale = m.m11;
        float subpixel = m.m21 - Math.round(m.m21);
        return ((float) Math.floor(value * scale) - subpixel) / scale;
    }

    public static float ceilX(Matrix3x2f m, float value) {
        if (m.m01 != 0.0f)
            return value;
        float scale = m.m00;
        float subpixel = m.m20 - Math.round(m.m20);
        return ((float) Math.ceil(value * scale) - subpixel) / scale;
    }

    public static float ceilY(Matrix3x2f m, float value) {
        if (m.m10 != 0.0f)
            return value;
        float scale = m.m11;
        float subpixel = m.m21 - Math.round(m.m21);
        return ((float) Math.ceil(value * scale) - subpixel) / scale;
    }
}
