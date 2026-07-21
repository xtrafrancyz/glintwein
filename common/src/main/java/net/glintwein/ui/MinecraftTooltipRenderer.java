package net.glintwein.ui;

import net.glintwein.Glintwein;
import net.glintwein.platform.Platform;
import net.glintwein.ui.data.Align;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.render.command.Context;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MinecraftTooltipRenderer {
    public static final String TOOLTIP_PREFIX = "$$glint$$";
    private static final Map<String, Function<String, Element>> FACTORIES = new HashMap<>();
    private static boolean initialized = false;

    private static TooltipContainer container;
    private static final Map<String, Element> factoryCache = new HashMap<>();
    private static final Map<Element, Vector2i> seenTooltips = new HashMap<>();

    private static void init() {
        if (!initialized) {
            initialized = true;
            Glintwein.addInitListener(() -> {
                container = new TooltipContainer();
                Glintwein.instance.layerAlwaysOnTop.getContent().addChild(container, 0);
            });
            Glintwein.addTickEndListener(() -> {
                if (container.getChildren().isEmpty())
                    return;
                for (Element el : container.getChildren()) {
                    if (!seenTooltips.containsKey(el)) {
                        container.removeChild(el);
                        factoryCache.values().remove(el);
                    }
                }
                if (seenTooltips.isEmpty())
                    factoryCache.clear();
                seenTooltips.clear();
            });
        }
    }

    public static boolean enabled() {
        return initialized;
    }

    public static void registerFactory(String namespace, Function<String, Element> factory) {
        if (FACTORIES.containsKey(namespace)) {
            throw new IllegalArgumentException("Tooltip factory for namespace '" + namespace + "' is already registered.");
        }
        FACTORIES.put(namespace, factory);
        init();
    }

    public static Element parseElement(CharSeqReader reader) {
        if (!initialized)
            return null;

        reader.run();
        String text = reader.getResult();
        if (text == null)
            return null;
        return parseElement(text);
    }

    public static Element parseElement(String text) {
        if (!initialized)
            return null;

        int idx = text.indexOf(":");
        if (idx == -1)
            return null;

        Element el = factoryCache.get(text);
        if (el == null) {
            String namespace = text.substring(0, idx);
            String data = text.substring(idx + 1);
            Function<String, Element> factory = FACTORIES.get(namespace);
            if (factory == null)
                return null;
            try {
                el = factory.apply(data);
            } catch (Exception e) {
                Platform.log().error("Error while creating tooltip for " + text, e);
                return null;
            }
            if (el != null)
                factoryCache.put(text, el);
        }

        if (el == null)
            return null;
        return el;
    }

    public static void setElementPos(Element el, int x, int y) {
        seenTooltips.put(el, new Vector2i(x, y));
        if (el.getParent() == null)
            container.addChild(el);
    }

    private static class TooltipContainer extends Element {
        private float scale;

        public TooltipContainer() {
            setWidthPercent(100);
            setHeightPercent(100);
            setPositionType(PositionType.ABSOLUTE);
            setPosition(Edge.TOP, 0);
            setPosition(Edge.LEFT, 0);
            setAlignItems(Align.FLEX_START);
        }

        @Override
        public void tick() {
            super.tick();

            float uiScale = GlobalUIState.getScale();
            float mcScale = Platform.get().getGuiScale();
            scale = mcScale / uiScale;
        }

        private boolean overrideChildrenPos() {
            if (seenTooltips.isEmpty())
                return false;
            for (Element el : getChildren()) {
                Vector2i pos = seenTooltips.get(el);
                if (pos != null) {
                    el.contentBox.x = pos.x + el.contentBox.x - el.borderBox.x;
                    el.contentBox.y = pos.y + el.contentBox.y - el.borderBox.y;
                    el.paddingBox.x = pos.x + el.paddingBox.x - el.borderBox.x;
                    el.paddingBox.y = pos.y + el.paddingBox.y - el.borderBox.y;
                    el.borderBox.x = pos.x;
                    el.borderBox.y = pos.y;
                }
            }
            return true;
        }

        @Override
        protected void handleMouseMoved(float mouseX, float mouseY, boolean canHover) {
            if (!overrideChildrenPos())
                return;
            super.handleMouseMoved(scaleMouse(mouseX), scaleMouse(mouseY), canHover);
        }

        @Override
        protected boolean handleMousePress(float mouseX, float mouseY, int button) {
            if (!overrideChildrenPos())
                return false;
            return super.handleMousePress(scaleMouse(mouseX), scaleMouse(mouseY), button);
        }

        @Override
        protected boolean handleMouseRelease(float mouseX, float mouseY, int button, boolean blocked) {
            if (!overrideChildrenPos())
                return false;
            return super.handleMouseRelease(scaleMouse(mouseX), scaleMouse(mouseY), button, blocked);
        }

        @Override
        protected boolean handleMouseScroll(float mouseX, float mouseY, float horizontal, float vertical) {
            if (!overrideChildrenPos())
                return false;
            return super.handleMouseScroll(scaleMouse(mouseX), scaleMouse(mouseY), horizontal, vertical);
        }

        private float scaleMouse(float pos) {
            return pos / scale;
        }

        @Override
        public void draw(Context ctx) {
            if (!overrideChildrenPos())
                return;

            ctx.pose().pushMatrix();
            ctx.pose().scale(scale);

            for (Element el : getChildren()) {
                if (!seenTooltips.containsKey(el))
                    continue;
                el.draw(ctx);
            }

            ctx.pose().popMatrix();
        }
    }

    public abstract static class CharSeqReader implements Runnable {
        private final StringBuilder builder = new StringBuilder();
        private int readCount = 0;
        private boolean foundGlint = false;

        protected boolean readChar(int codePoint) {
            builder.appendCodePoint(codePoint);
            readCount++;
            if (readCount == 9) {
                if (!TOOLTIP_PREFIX.contentEquals(builder))
                    return false;
                builder.setLength(0);
                foundGlint = true;
            }
            return true;
        }

        public String getResult() {
            return foundGlint ? builder.toString() : null;
        }
    }
}
