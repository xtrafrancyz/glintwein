package net.glintwein.impl;

import net.glintwein.Glintwein;
import net.glintwein.platform.Platform;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.Align;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.render.command.Context;
import net.minecraft.util.FormattedCharSequence;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TooltipInterceptor {
    public static final String TOOLTIP_PREFIX = "$$glint$$";
    private static final Map<String, Function<String, Element>> FACTORIES = new HashMap<>();
    private static boolean initialized = false;

    private static TooltipContainer container;
    private static final Map<String, Element> factoryCache = new HashMap<>();
    private static final Map<Element, Integer> seenTooltips = new HashMap<>();
    private static final Map<FormattedCharSequence, Element> parsedCache = new HashMap<>();
    private static int tooltipBaseX = 0;
    private static int tooltipBaseY = 0;

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
                parsedCache.clear();
            });
        }
    }

    public static void registerTooltipFactory(String namespace, Function<String, Element> factory) {
        if (FACTORIES.containsKey(namespace)) {
            throw new IllegalArgumentException("Tooltip factory for namespace '" + namespace + "' is already registered.");
        }
        FACTORIES.put(namespace, factory);
        init();
    }

    public static Element parseElement(FormattedCharSequence seq) {
        if (!initialized)
            return null;

        StringBuilder sb = new StringBuilder();
        boolean[] state = {
            true, // waiting for "$$glint$$"
            false // glint found
        };
        seq.accept((i, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            if (state[0]) {
                if (sb.length() >= 9) {
                    state[0] = false;
                    if (!TOOLTIP_PREFIX.contentEquals(sb))
                        return false;
                    sb.setLength(0);
                    state[1] = true;
                }
            }
            return true;
        });
        if (!state[1])
            return null;
        String text = sb.toString();

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
        parsedCache.put(seq, el);
        return el;
    }

    public static Element getCachedElement(FormattedCharSequence seq) {
        return parsedCache.get(seq);
    }

    public static void setElementY(Element el, int y) {
        seenTooltips.put(el, y);
        if (el.getParent() == null)
            container.addChild(el);
    }

    public static void setTooltipBase(int x, int y) {
        tooltipBaseX = x;
        tooltipBaseY = y;
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
                Integer yPos = seenTooltips.get(el);
                if (yPos != null) {
                    el.contentBox.x = tooltipBaseX + el.contentBox.x - el.borderBox.x;
                    el.contentBox.y = tooltipBaseY + yPos + el.contentBox.y - el.borderBox.y;
                    el.paddingBox.x = tooltipBaseX + el.paddingBox.x - el.borderBox.x;
                    el.paddingBox.y = tooltipBaseY + yPos + el.paddingBox.y - el.borderBox.y;
                    el.borderBox.x = tooltipBaseX;
                    el.borderBox.y = tooltipBaseY + yPos;
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
}
