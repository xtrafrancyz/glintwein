package net.glintwein.ui.element;

import org.lwjgl.util.yoga.Yoga;

public class RootElement extends Element {
    private float lastWidth = -321;
    private float lastHeight = -321;

    public void calculateLayout(float width, float height) {
        if (width == lastWidth && height == lastHeight && !Yoga.YGNodeIsDirty(yogaNode))
            return;
        lastWidth = width;
        lastHeight = height;

        if (width < 0)
            width = Yoga.YGUndefined;
        if (height < 0)
            height = Yoga.YGUndefined;
        Yoga.YGNodeCalculateLayout(yogaNode, width, height, Yoga.YGDirectionLTR);

        propagateLayoutRead();
    }
}
