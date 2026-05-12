package net.glintwein.ui.element.component;

import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.Element;
import org.joml.Vector2f;

import java.util.function.Function;

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
    protected boolean handleMouseRelease(float mouseX, float mouseY, int button, boolean blocked) {
        if (!isPressed())
            getParent().removeChild(this);
        return super.handleMouseRelease(mouseX, mouseY, button, blocked);
    }

    public static Dropdown openBelow(Element triggerElement) {
        Bounds absoluteBorderBox = triggerElement.getAbsoluteBorderBox();
        Dropdown dropdown = new Dropdown(absoluteBorderBox.minX, absoluteBorderBox.maxY);
        triggerElement.getRoot().addChild(dropdown);
        return dropdown;
    }

    public static Dropdown openBelowRelative(Element triggerElement, Element parent) {
        Dropdown dropdown = new Dropdown.Ticking(dd -> {
            Bounds triggerBox = triggerElement.getRelativeBorderBox(parent);
            return new Vector2f(triggerBox.minX, triggerBox.maxY);
        });
        parent.addChild(dropdown);
        return dropdown;
    }

    public static Dropdown openCenteredBelowRelative(Element triggerElement, Element parent) {
        Ticking dropdown = new Dropdown.Ticking(dd -> {
            float ddw = dd.getComputedWidth();
            Bounds triggerBox = triggerElement.getRelativeBorderBox(parent);
            float x = triggerBox.minX + (triggerBox.maxX - triggerBox.minX - ddw) / 2;
            return new Vector2f(x, triggerBox.maxY);
        });
        dropdown.delayVisibility();
        parent.addChild(dropdown);
        return dropdown;
    }

    public static class Ticking extends Dropdown {
        private final Function<Dropdown, Vector2f> positionGetter;
        private float lastX, lastY;
        private int ticks;

        public Ticking(Function<Dropdown, Vector2f> positionGetter) {
            super(0, 0);
            this.positionGetter = positionGetter;
        }

        /**
         * Необходимо для того чтобы dropdown не появлялся на неправильных координатах
         * в первом кадре, когда его размер еще не вычислен, а координаты уже запрашиваются
         */
        public void delayVisibility() {
            setOpacity(0);
        }

        @Override
        public void tick() {
            if (ticks == 1)
                setOpacity(1);
            if (ticks < 100) // overflow protection
                ticks++;

            super.tick();
            update();
        }

        public void update() {
            Vector2f pos = positionGetter.apply(this);
            if (pos.x != lastX) {
                setPosition(Edge.LEFT, pos.x);
                lastX = pos.x;
            }
            if (pos.y != lastY) {
                setPosition(Edge.TOP, pos.y);
                lastY = pos.y;
            }
        }
    }
}
