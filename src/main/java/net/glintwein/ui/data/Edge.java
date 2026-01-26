package net.glintwein.ui.data;

import org.lwjgl.util.yoga.Yoga;

public enum Edge {
    LEFT(Yoga.YGEdgeLeft),
    TOP(Yoga.YGEdgeTop),
    RIGHT(Yoga.YGEdgeRight),
    BOTTOM(Yoga.YGEdgeBottom),
    START(Yoga.YGEdgeStart),
    END(Yoga.YGEdgeEnd),
    HORIZONTAL(Yoga.YGEdgeHorizontal),
    VERTICAL(Yoga.YGEdgeVertical),
    ALL(Yoga.YGEdgeAll);

    private final int value;

    Edge(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
