package net.glintwein.ui.data;

import org.lwjgl.util.yoga.Yoga;

public enum Wrap {
    NO_WRAP(Yoga.YGWrapNoWrap),
    WRAP(Yoga.YGWrapWrap),
    WRAP_REVERSE(Yoga.YGWrapReverse);

    final int value;

    Wrap(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
