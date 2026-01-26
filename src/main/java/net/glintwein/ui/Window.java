package net.glintwein.ui;

import net.glintwein.GlintweinFabricMod;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.RootElement;
import net.glintwein.ui.element.YogaNode;
import net.glintwein.ui.render.command.Context;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.yoga.YGValue;
import org.lwjgl.util.yoga.Yoga;

public class Window {
    public RootElement root;

    private boolean dragged;
    private float dragStartX;
    private float dragStartY;

    public Window() {
        root = new RootElement();
        root.setPositionType(PositionType.ABSOLUTE);
        root.setPosition(Edge.LEFT, 0);
        root.setPosition(Edge.TOP, 0);
    }

    public void tick() {
        updateMouse(GlintweinFabricMod.getMouseX(), GlintweinFabricMod.getMouseY());
        root.tick();
        root.calculateLayout(-1, -1);
    }

    public void draw(Context ctx) {
        root.draw(ctx);
    }

    private void updateMouse(float mouseX, float mouseY) {
        if (dragged) {
            float deltaX = mouseX - dragStartX;
            float deltaY = mouseY - dragStartY;

            try (MemoryStack stack = MemoryStack.stackPush()) {
                YGValue result = YGValue.mallocStack(stack);
                long yogaNode = YogaNode.getYogaNodeHandle(root);

                Yoga.YGNodeStyleGetPosition(yogaNode, Yoga.YGEdgeLeft, result);
                root.setPosition(Edge.LEFT, result.value() + deltaX);

                Yoga.YGNodeStyleGetPosition(yogaNode, Yoga.YGEdgeTop, result);
                root.setPosition(Edge.TOP, result.value() + deltaY);
            }

            dragStartX = mouseX;
            dragStartY = mouseY;
        }
        root.updateMouse(mouseX, mouseY);
    }

    public boolean onMousePress(float mouseX, float mouseY, int button) {
        boolean handled = root.handleMousePress(mouseX, mouseY, button);

        if (!handled && root.isHovered()) {
            dragged = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            handled = true;
        }

        return handled;
    }

    public boolean onMouseRelease(float mouseX, float mouseY, int button) {
        dragged = false;
        return root.handleMouseRelease(mouseX, mouseY, button);
    }

    public boolean onMouseScroll(float mouseX, float mouseY, float amount, float vertical) {
        return root.handleMouseScroll(mouseX, mouseY, amount, vertical);
    }
}
