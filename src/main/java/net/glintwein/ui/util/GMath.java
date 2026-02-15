package net.glintwein.ui.util;

import net.minecraft.util.Mth;

public class GMath {
    public static final float PI = 3.1415927f;
    public static final float HALF_PI = PI / 2;

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
		int i = (int)value;
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
