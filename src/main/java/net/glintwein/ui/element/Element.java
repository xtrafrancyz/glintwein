package net.glintwein.ui.element;

import net.glintwein.ui.data.Box;
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
    protected final List<Animated> animations = new ArrayList<>();

    private int backgroundColor = 0x00000000;

    public void setBackground(int color) {
        this.backgroundColor = color;
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

    protected void propagateLayoutRead() {
        if (Yoga.YGNodeGetHasNewLayout(yogaNode)) {
            Yoga.YGNodeSetHasNewLayout(yogaNode, false);
            readLayout();
        }

        for (Element child : children)
            child.propagateLayoutRead();
    }

    private void readLayout() {
        borderBox.x = Yoga.YGNodeLayoutGetLeft(yogaNode);
        borderBox.y = Yoga.YGNodeLayoutGetTop(yogaNode);
        borderBox.width = Yoga.YGNodeLayoutGetWidth(yogaNode);
        borderBox.height = Yoga.YGNodeLayoutGetHeight(yogaNode);
        float paddingLeft = Yoga.YGNodeLayoutGetPadding(yogaNode, Yoga.YGEdgeLeft);
        float paddingTop = Yoga.YGNodeLayoutGetPadding(yogaNode, Yoga.YGEdgeTop);
        paddingBox.x = borderBox.x + paddingLeft;
        paddingBox.y = borderBox.y + paddingTop;
        paddingBox.width = borderBox.width - paddingLeft - Yoga.YGNodeLayoutGetPadding(yogaNode, Yoga.YGEdgeRight);
        paddingBox.height = borderBox.height - paddingTop - Yoga.YGNodeLayoutGetPadding(yogaNode, Yoga.YGEdgeBottom);
        float borderLeft = Yoga.YGNodeLayoutGetBorder(yogaNode, Yoga.YGEdgeLeft);
        float borderTop = Yoga.YGNodeLayoutGetBorder(yogaNode, Yoga.YGEdgeTop);
        contentBox.x = paddingBox.x + borderLeft;
        contentBox.y = paddingBox.y + borderTop;
        contentBox.width = paddingBox.width - borderLeft - Yoga.YGNodeLayoutGetBorder(yogaNode, Yoga.YGEdgeRight);
        contentBox.height = paddingBox.height - borderTop - Yoga.YGNodeLayoutGetBorder(yogaNode, Yoga.YGEdgeBottom);
        if (layoutLerp != null)
            layoutLerp.animate(borderBox, paddingBox, contentBox);
    }

    public void trackAnimation(Animated animation) {
        animations.add(animation);
    }

    public void tick() {
        for (Animated animation : animations)
            animation.update();
        for (Element child : children)
            child.tick();
        if (layoutLerp != null)
            layoutLerp.apply(borderBox, paddingBox, contentBox);
    }

    public void draw(Context context) {
        if (ARGB.alpha(backgroundColor) > 0)
            context.drawRect(borderBox.x, borderBox.y, borderBox.width, borderBox.height, backgroundColor);

        for (Element child : children)
            child.draw(context);
    }
}
