package net.glintwein.ui.data;

import org.lwjgl.util.yoga.Yoga;

public enum PositionType {
    RELATIVE(Yoga.YGPositionTypeRelative),
    ABSOLUTE(Yoga.YGPositionTypeAbsolute);

    private final int value;

    PositionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
