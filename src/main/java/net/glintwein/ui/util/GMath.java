package net.glintwein.ui.util;

import net.minecraft.util.Mth;

public class GMath {
    public static final float PI = (float) Math.PI;
    public static final float HALF_PI = (float) (Math.PI / 2.0);
    public static final float TWO_PI = (float) (Math.PI * 2.0);
    public static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
    public static final float RAD_TO_DEG = 180.0F / (float) Math.PI;

    public static float sin(float value) {
        return Mth.sin(value);
    }

    public static float cos(float value) {
        return Mth.cos(value);
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
}
