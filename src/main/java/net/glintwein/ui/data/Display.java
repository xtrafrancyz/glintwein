package net.glintwein.ui.data;

import org.lwjgl.util.yoga.Yoga;

public enum Display {
    FLEX(Yoga.YGDisplayFlex),
    NONE(Yoga.YGDisplayNone);

    private final int value;

    Display(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
