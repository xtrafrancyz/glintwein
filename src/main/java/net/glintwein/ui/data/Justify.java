package net.glintwein.ui.data;

import org.lwjgl.util.yoga.Yoga;

public enum Justify {
    FLEX_START(Yoga.YGJustifyFlexStart),
    CENTER(Yoga.YGJustifyCenter),
    FLEX_END(Yoga.YGJustifyFlexEnd),
    SPACE_BETWEEN(Yoga.YGJustifySpaceBetween),
    SPACE_AROUND(Yoga.YGJustifySpaceAround),
    SPACE_EVENLY(Yoga.YGJustifySpaceEvenly);

    private final int value;

    Justify(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
