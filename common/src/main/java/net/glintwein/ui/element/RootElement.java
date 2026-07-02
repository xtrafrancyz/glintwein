package net.glintwein.ui.element;

import net.glintwein.platform.Platform;

public class RootElement extends Element {
    private float lastMouseX = -321;
    private float lastMouseY = -321;
    private float lastWidth = -321;
    private float lastHeight = -321;
    private boolean layoutChanged = true;

    public void calculateLayout(float width, float height) {
        if (width == lastWidth && height == lastHeight && !Platform.yoga().NodeIsDirty(yogaNode))
            return;
        layoutChanged = true;
        lastWidth = width;
        lastHeight = height;

        if (width < 0)
            width = Float.NaN; //Yoga.YGUndefined;
        if (height < 0)
            height = Float.NaN; //Yoga.YGUndefined;
        Platform.yoga().NodeCalculateLayout(yogaNode, width, height);

        propagateLayoutRead();
    }

    public void updateMouse(float mouseX, float mouseY, boolean canHover) {
        if (lastWidth == -321)
            return;
        if (mouseX == lastMouseX && mouseY == lastMouseY && !layoutChanged)
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
        // TODO support for new yoga without this method
        Platform.yoga().NodeMarkDirtyAndPropogateToDescendants(getYogaNodeHandle(this));
    }
}
