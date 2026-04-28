package net.glintwein.ui.element.component;

import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.render.command.Context;

public class Dropdown extends Element {
    public Dropdown(float x, float y) {
        setWidth(200);
        setPositionType(PositionType.ABSOLUTE);
        setPosition(Edge.LEFT, x);
        setPosition(Edge.TOP, y);
    }

    public void close() {
        Element parent = getParent();
        if (parent != null)
            parent.removeChild(this);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static Dropdown openBelow(Element triggerElement) {
        Bounds absoluteBorderBox = triggerElement.getAbsoluteBorderBox();
        Dropdown dropdown = new Dropdown(absoluteBorderBox.minX, absoluteBorderBox.maxY);
        triggerElement.getRoot().addChild(dropdown);
        return dropdown;
    }
}
