package net.glintwein.ui;

import net.glintwein.impl.TooltipInterceptor;
import net.glintwein.ui.element.Element;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.function.Function;

public class GlintweinTooltip {
    public static void register(String namespace, Function<String, Element> factory) {
        TooltipInterceptor.registerTooltipFactory(namespace, factory);
    }

    public static Component createKey(String namespace, String date) {
        return new TextComponent(TooltipInterceptor.TOOLTIP_PREFIX + namespace + ":" + date);
    }
}
