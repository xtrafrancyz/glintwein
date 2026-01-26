package net.glintwein.ui.data;

import org.lwjgl.util.yoga.Yoga;

public enum Overflow {
    VISIBLE(Yoga.YGOverflowVisible),
    HIDDEN(Yoga.YGOverflowHidden),
    SCROLL(Yoga.YGOverflowScroll);

    private final int value;

    Overflow(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
