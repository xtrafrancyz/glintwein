package net.glintwein.ui.element;

import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.Overflow;
import net.glintwein.ui.data.PositionType;
import org.lwjgl.util.yoga.Yoga;

public class Scrollable extends Element {
    private final Element content = new Element();

    private boolean hasOverflow;

    public Scrollable() {
        setOverflow(Overflow.SCROLL);
        addChild(content);
    }

    public Element getContent() {
        return content;
    }

    public void createVerticalScrollbar() {
        Element thumb = new Element();
        thumb.setPositionType(PositionType.ABSOLUTE);
        thumb.setPosition(Edge.RIGHT, 1);
        thumb.setWidth(5);
        addChild(thumb);
    }

    @Override
    protected void readYogaLayout() {
        super.readYogaLayout();
        hasOverflow = Yoga.YGNodeLayoutGetHadOverflow(yogaNode);
    }
}
