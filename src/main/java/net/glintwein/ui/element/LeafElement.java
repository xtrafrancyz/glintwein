package net.glintwein.ui.element;

import org.lwjgl.util.yoga.Yoga;

import java.util.Collections;
import java.util.List;

public class LeafElement extends Element {
    @Override
    @Deprecated
    public List<Element> getChildren() {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public void addChild(Element child) {
        throw new UnsupportedOperationException("LeafElement cannot have children");
    }

    @Override
    @Deprecated
    public void addChild(Element child, int index) {
        throw new UnsupportedOperationException("LeafElement cannot have children");
    }

    @Override
    @Deprecated
    public void removeChild(Element child) {
        throw new UnsupportedOperationException("LeafElement cannot have children");
    }

    @Override
    @Deprecated
    public void clearChildren() {
        // No-op
    }

    protected void markDirty() {
        Yoga.YGNodeMarkDirty(yogaNode);
    }
}
