package net.glintwein.ui.element;

import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Box;
import net.glintwein.ui.data.Display;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.util.ARGB;
import net.glintwein.ui.util.Animated;
import net.glintwein.ui.util.Easing;
import net.glintwein.ui.util.LayoutBoxLerp;
import org.lwjgl.util.yoga.Yoga;

import java.util.ArrayList;
import java.util.List;

public class Element extends YogaNode {
    private final List<Element> children = new ArrayList<>();
    protected final Box borderBox = new Box();
    protected final Box paddingBox = new Box();
    protected final Box contentBox = new Box();
    private LayoutBoxLerp layoutLerp;
    private final List<Animated> animations = new ArrayList<>();

    private boolean hovered;
    private boolean pressed;

    private int backgroundColor = 0x00000000;
    protected BorderRadius borderRadius = BorderRadius.ZERO;
    private float opacity = 1.0f;
    private ClickHandler clickHandler;
    private MousePressHandler mousePressHandler;
    private MouseReleaseHandler mouseReleaseHandler;
    private MouseScrollHandler mouseScrollHandler;

    public void setBackground(int color) {
        this.backgroundColor = color;
    }

    public void setBorderRadius(BorderRadius radius) {
        this.borderRadius = radius;
    }

    public void setClickHandler(ClickHandler handler) {
        this.clickHandler = handler;
    }

    public void setMousePressHandler(MousePressHandler handler) {
        this.mousePressHandler = handler;
    }

    public void setMouseReleaseHandler(MouseReleaseHandler handler) {
        this.mouseReleaseHandler = handler;
    }

    public void setMouseScrollHandler(MouseScrollHandler handler) {
        this.mouseScrollHandler = handler;
    }

    public void trackAnimation(Animated animation) {
        animations.add(animation);
    }

    public void enableLayoutLerp(float durationMs, Easing easing) {
        layoutLerp = new LayoutBoxLerp(durationMs, easing);
    }

    public void disableLayoutLerp() {
        layoutLerp = null;
    }

    public void addChild(Element child) {
        Yoga.YGNodeInsertChild(yogaNode, child.yogaNode, children.size());
        children.add(child);
    }

    public void addChild(Element child, int index) {
        Yoga.YGNodeInsertChild(yogaNode, child.yogaNode, index);
        children.add(index, child);
    }

    public void removeChild(Element child) {
        int index = children.indexOf(child);
        if (index == -1)
            return;
        Yoga.YGNodeRemoveChild(yogaNode, child.yogaNode);
        children.remove(index);
    }

    public List<Element> getChildren() {
        return children;
    }

    public void clearChildren() {
        Yoga.YGNodeRemoveAllChildren(yogaNode);
        children.clear();
    }

    public float getComputedWidth() {
        return borderBox.width;
    }

    public float getComputedHeight() {
        return borderBox.height;
    }

    protected void propagateLayoutRead() {
        if (Yoga.YGNodeGetHasNewLayout(yogaNode)) {
            Yoga.YGNodeSetHasNewLayout(yogaNode, false);
            readYogaLayout();
        }

        for (Element child : children)
            child.propagateLayoutRead();
    }

    protected void readYogaLayout() {
        // The Border Box (Outer dimensions)
        borderBox.x = Yoga.YGNodeLayoutGetLeft(yogaNode);
        borderBox.y = Yoga.YGNodeLayoutGetTop(yogaNode);
        borderBox.width = Yoga.YGNodeLayoutGetWidth(yogaNode);
        borderBox.height = Yoga.YGNodeLayoutGetHeight(yogaNode);

        // The Padding Box
        float borderLeft = Yoga.YGNodeLayoutGetBorder(yogaNode, Yoga.YGEdgeLeft);
        float borderTop = Yoga.YGNodeLayoutGetBorder(yogaNode, Yoga.YGEdgeTop);
        paddingBox.x = borderBox.x + borderLeft;
        paddingBox.y = borderBox.y + borderTop;
        paddingBox.width = borderBox.width - borderLeft - Yoga.YGNodeLayoutGetBorder(yogaNode, Yoga.YGEdgeRight);
        paddingBox.height = borderBox.height - borderTop - Yoga.YGNodeLayoutGetBorder(yogaNode, Yoga.YGEdgeBottom);

        // The Content Box
        float paddingLeft = Yoga.YGNodeLayoutGetPadding(yogaNode, Yoga.YGEdgeLeft);
        float paddingTop = Yoga.YGNodeLayoutGetPadding(yogaNode, Yoga.YGEdgeTop);
        contentBox.x = paddingBox.x + paddingLeft;
        contentBox.y = paddingBox.y + paddingTop;
        contentBox.width = paddingBox.width - paddingLeft - Yoga.YGNodeLayoutGetPadding(yogaNode, Yoga.YGEdgeRight);
        contentBox.height = paddingBox.height - paddingTop - Yoga.YGNodeLayoutGetPadding(yogaNode, Yoga.YGEdgeBottom);

        if (layoutLerp != null)
            layoutLerp.animate(borderBox, paddingBox, contentBox);
    }

    protected void handleMouseMoved(float mouseX, float mouseY, boolean canHover) {
        hovered = canHover && borderBox.contains(mouseX, mouseY);

        float localX = mouseX - borderBox.x;
        float localY = mouseY - borderBox.y;
        for (Element child : children) {
            child.handleMouseMoved(localX, localY, canHover);
        }
    }

    protected boolean handleMousePress(float mouseX, float mouseY, int button) {
        pressed = true;
        float localX = mouseX - borderBox.x;
        float localY = mouseY - borderBox.y;
        boolean handled = false;
        for (Element child : children) {
            if (child.canHandleClick() && child.handleMousePress(localX, localY, button)) {
                handled = true;
                break;
            }
        }
        if (!handled && mousePressHandler != null)
            handled = mousePressHandler.onMousePress(localX, localY, button);
        if (isFocusable())
            GlobalUIState.requestFocus(this);
        return handled;
    }

    protected boolean handleMouseRelease(float mouseX, float mouseY, int button, boolean blocked) {
        float localX = mouseX - borderBox.x;
        float localY = mouseY - borderBox.y;
        for (Element child : children) {
            if (child.handleMouseRelease(localX, localY, button, blocked))
                blocked = true;
        }
        if (pressed) {
            pressed = false;
            if (mouseReleaseHandler != null)
                blocked |= mouseReleaseHandler.onMouseRelease(localX, localY, button);
            if (hovered && !blocked && clickHandler != null)
                blocked = clickHandler.onClick(button);
        }
        return blocked;
    }

    protected boolean handleMouseScroll(float mouseX, float mouseY, float horizontal, float vertical) {
        float localX = mouseX - borderBox.x;
        float localY = mouseY - borderBox.y;
        boolean handled = false;
        for (Element child : children) {
            if (child.canHandleClick() && child.handleMouseScroll(localX, localY, horizontal, vertical)) {
                handled = true;
                break;
            }
        }
        if (!handled && mouseScrollHandler != null)
            handled = mouseScrollHandler.onMouseScroll(localX, localY, horizontal, vertical);
        return handled;
    }

    public boolean canHandleClick() {
        return getDisplayType() == Display.FLEX && hovered;
    }

    public boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean handleCharTyped(char character, int keyCode) {
        return false;
    }

    public void handleFocusGain() {
    }

    public void handleFocusLoss() {
    }

    public boolean isFocusable() {
        return false;
    }

    public boolean isInFocus() {
        return GlobalUIState.getFocusedElement() == this;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void tick() {
        GlobalUIState.tickElement(this);
        for (Animated animation : animations)
            animation.update();
        for (Element child : children)
            child.tick();
        if (layoutLerp != null)
            layoutLerp.apply(borderBox, paddingBox, contentBox);
    }

    public void draw(Context ctx) {
        if (opacity != 1.0f) {
            if (ctx.pushOpacity(opacity) == 0) {
                ctx.popOpacity();
                return;
            }
        }

        if (ARGB.alpha(backgroundColor) > 0)
            ctx.drawRect(borderBox, borderRadius, backgroundColor);

        if (!children.isEmpty()) {
            ctx.pose().pushMatrix();
            ctx.pose().translate(borderBox.x, borderBox.y);
            for (Element child : children)
                child.draw(ctx);
            ctx.pose().popMatrix();
        }

        if (opacity != 1)
            ctx.popOpacity();
    }

    public boolean isHovered() {
        return hovered;
    }

    public interface ClickHandler {
        boolean onClick(int button);
    }

    public interface MousePressHandler {
        boolean onMousePress(float mouseX, float mouseY, int button);
    }

    public interface MouseReleaseHandler {
        boolean onMouseRelease(float mouseX, float mouseY, int button);
    }

    public interface MouseScrollHandler {
        boolean onMouseScroll(float mouseX, float mouseY, float amount, float vertical);
    }
}
