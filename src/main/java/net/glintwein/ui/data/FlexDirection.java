package net.glintwein.ui.data;

import org.lwjgl.util.yoga.Yoga;

public enum FlexDirection {
    COLUMN(Yoga.YGFlexDirectionColumn),
    COLUMN_REVERSE(Yoga.YGFlexDirectionColumnReverse),
    ROW(Yoga.YGFlexDirectionRow),
    ROW_REVERSE(Yoga.YGFlexDirectionRowReverse);

    private final int value;

    FlexDirection(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
