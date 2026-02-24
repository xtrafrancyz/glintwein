package net.glintwein.ui.element;

import org.lwjgl.util.yoga.Yoga;

public class RootElement extends Element {
    private float lastMouseX = -321;
    private float lastMouseY = -321;
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

    public void updateMouse(float mouseX, float mouseY, boolean canHover) {
        if (lastWidth == -321)
            return;
        if (mouseX == lastMouseX && mouseY == lastMouseY)
            return;
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        handleMouseMoved(mouseX, mouseY, canHover);
    }

    @Override
    public boolean handleMousePress(float mouseX, float mouseY, int button) {
        return super.handleMousePress(mouseX, mouseY, button);
    }

    public boolean handleMouseRelease(float mouseX, float mouseY, int button) {
        return super.handleMouseRelease(mouseX, mouseY, button, false);
    }

    @Override
    public boolean handleMouseScroll(float mouseX, float mouseY, float horizontal, float vertical) {
        return super.handleMouseScroll(mouseX, mouseY, horizontal, vertical);
    }

    public void invalidateLayout() {
        Yoga.nYGNodeMarkDirtyAndPropogateToDescendants(getYogaNodeHandle(this));
    }
}
