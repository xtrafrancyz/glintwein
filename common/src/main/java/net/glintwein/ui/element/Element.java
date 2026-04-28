package net.glintwein.ui.element;

import net.glintwein.platform.Platform;
import net.glintwein.platform.YogaLayoutResult;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.data.Box;
import net.glintwein.ui.data.Display;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Element extends YogaNode {
    private Element parent;
    private final List<Element> children = new CopyOnWriteArrayList<>();
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

    private ClickHandler onClick;
    private MousePressHandler onMousePress;
    private MouseReleaseHandler onMouseRelease;
    private MouseScrollHandler onMouseScroll;
    private Runnable onMouseEnter;
    private Runnable onMouseExit;

    public void setBackground(int color) {
        this.backgroundColor = color;
    }

    public void setBorderRadius(BorderRadius radius) {
        this.borderRadius = radius;
    }

    public void setOpacity(float opacity) {
        this.opacity = GMath.clamp(opacity, 0, 1);
    }

    public void setOnClick(ClickHandler handler) {
        this.onClick = handler;
    }

    public void setOnMousePress(MousePressHandler handler) {
        this.onMousePress = handler;
    }

    public void setOnMouseRelease(MouseReleaseHandler handler) {
        this.onMouseRelease = handler;
    }

    public void setOnMouseScroll(MouseScrollHandler handler) {
        this.onMouseScroll = handler;
    }

    public void setOnMouseEnter(Runnable handler) {
        this.onMouseEnter = handler;
    }

    public void setOnMouseExit(Runnable handler) {
        this.onMouseExit = handler;
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
        Platform.yoga().NodeInsertChild(yogaNode, child.yogaNode, children.size());
        children.add(child);
        child.parent = this;
    }

    public void addChild(Element child, int index) {
        Platform.yoga().NodeInsertChild(yogaNode, child.yogaNode, index);
        children.add(index, child);
        child.parent = this;
    }

    public void removeChild(Element child) {
        int index = children.indexOf(child);
        if (index == -1)
            return;
        Platform.yoga().NodeRemoveChild(yogaNode, child.yogaNode);
        children.remove(index);
        child.parent = null;
    }

    public List<Element> getChildren() {
        return children;
    }

    public void clearChildren() {
        Platform.yoga().NodeRemoveAllChildren(yogaNode);
        for (Element child : children)
            child.parent = null;
        children.clear();
    }

    public float getComputedWidth() {
        return borderBox.width;
    }

    public float getComputedHeight() {
        return borderBox.height;
    }

    public Bounds getAbsoluteBorderBox() {
        float absX = borderBox.x;
        float absY = borderBox.y;
        Element current = parent;
        while (current != null) {
            absX += current.borderBox.x;
            absY += current.borderBox.y;
            current = current.parent;
        }
        return Bounds.fromXYWH(absX, absY, borderBox.width, borderBox.height);
    }

    protected void propagateLayoutRead() {
        if (Platform.yoga().NodeGetHasNewLayout(yogaNode)) {
            Platform.yoga().NodeSetHasNewLayout(yogaNode, false);
            readYogaLayout();
        }

        for (Element child : children)
            child.propagateLayoutRead();
    }

    protected void readYogaLayout() {
        YogaLayoutResult result = Platform.yoga().NodeGetLayout(yogaNode);
        borderBox.set(result.border);
        paddingBox.set(result.padding);
        contentBox.set(result.content);
        if (layoutLerp != null)
            layoutLerp.animate(borderBox, paddingBox, contentBox);
    }

    public Element getSharedParent(Element other) {
        Element sharedParent = null;
        Element current = this;
        outer:
        while (current != null) {
            Element otherCurrent = other;
            while (otherCurrent != null) {
                if (current == otherCurrent) {
                    sharedParent = current;
                    break outer;
                }
                otherCurrent = otherCurrent.parent;
            }
            current = current.parent;
        }
        return sharedParent;
    }

    public Element getRoot() {
        Element root = this;
        while (root.parent != null)
            root = root.parent;
        return root;
    }

    public Element getParent() {
        return parent;
    }

    protected void handleMouseMoved(float mouseX, float mouseY, boolean canHover) {
        boolean wasHovered = hovered;
        hovered = canHover && getDisplayType() != Display.NONE && borderBox.contains(mouseX, mouseY);
        if (wasHovered != hovered) {
            if (hovered && onMouseEnter != null)
                onMouseEnter.run();
            else if (!hovered && onMouseExit != null)
                onMouseExit.run();
        }

        float localX = mouseX - borderBox.x;
        float localY = mouseY - borderBox.y;
        for (int i = children.size() - 1; i >= 0; i--) {
            Element child = children.get(i);
            child.handleMouseMoved(localX, localY, canHover);
            if (child.hovered)
                canHover = false;
        }
    }

    protected boolean handleMousePress(float mouseX, float mouseY, int button) {
        pressed = true;
        float localX = mouseX - borderBox.x;
        float localY = mouseY - borderBox.y;
        boolean handled = false;
        for (int i = children.size() - 1; i >= 0; i--) {
            Element child = children.get(i);
            if (child.canHandleClick() && child.handleMousePress(localX, localY, button)) {
                handled = true;
                break;
            }
        }
        if (!handled && onMousePress != null)
            handled = onMousePress.onMousePress(localX, localY, button);
        if (onClick != null)
            handled = true;
        if (isFocusable())
            GlobalUIState.requestFocus(this);
        return handled;
    }

    protected boolean handleMouseRelease(float mouseX, float mouseY, int button, boolean blocked) {
        float localX = mouseX - borderBox.x;
        float localY = mouseY - borderBox.y;
        for (int i = children.size() - 1; i >= 0; i--) {
            Element child = children.get(i);
            if (child.handleMouseRelease(localX, localY, button, blocked))
                blocked = true;
        }
        if (pressed) {
            pressed = false;
            if (onMouseRelease != null)
                blocked |= onMouseRelease.onMouseRelease(localX, localY, button);
            if (hovered && !blocked && onClick != null)
                blocked = onClick.onClick(button);
        }
        return blocked;
    }

    protected boolean handleMouseScroll(float mouseX, float mouseY, float horizontal, float vertical) {
        float localX = mouseX - borderBox.x;
        float localY = mouseY - borderBox.y;
        boolean handled = false;
        for (int i = children.size() - 1; i >= 0; i--) {
            Element child = children.get(i);
            if (child.canHandleClick() && child.handleMouseScroll(localX, localY, horizontal, vertical)) {
                handled = true;
                break;
            }
        }
        if (!handled && onMouseScroll != null)
            handled = onMouseScroll.onMouseScroll(localX, localY, horizontal, vertical);
        return handled;
    }

    public boolean canHandleClick() {
        return getDisplayType() != Display.NONE && hovered;
    }

    public boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean handleCharTyped(char character, int modifiers) {
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
        if (getDisplayType() == Display.NONE)
            return;
        GlobalUIState.tickElement(this);
        for (Animated animation : animations)
            animation.update();
        for (Element child : children)
            child.tick();
        if (layoutLerp != null)
            layoutLerp.apply(borderBox, paddingBox, contentBox);
    }

    public void draw(Context ctx) {
        if (getDisplayType() == Display.NONE)
            return;
        if (opacity != 1.0f) {
            if (ctx.pushOpacity(opacity) == 0) {
                ctx.popOpacity();
                return;
            }
        }

        if (ARGB.alpha(backgroundColor) > 0)
            ctx.drawRect(borderBox, borderRadius, backgroundColor);

        drawContent(ctx);

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

    protected void drawContent(Context ctx) {
        // for override
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
