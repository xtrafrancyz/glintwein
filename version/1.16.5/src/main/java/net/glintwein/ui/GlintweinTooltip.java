package net.glintwein.ui;

import net.glintwein.impl.TooltipInterceptor;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.render.font.SizedFont;
import net.glintwein.ui.rtf.RichContent;
import net.glintwein.ui.util.ARGB;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GlintweinTooltip {
    public static void register(String namespace, Function<String, Element> factory) {
        TooltipInterceptor.registerTooltipFactory(namespace, factory);
    }

    public static Component createKey(String namespace, String date) {
        return new TextComponent(TooltipInterceptor.TOOLTIP_PREFIX + namespace + ":" + date);
    }

    public static RichContent mcToRichContent(Component component) {
        return mcToRichContent(component, null, null, null);
    }

    public static RichContent mcToRichContent(Component component, SizedFont regular, SizedFont bold, SizedFont italic) {
        RichContent.Builder output = new RichContent.Builder();
        mcToRichContentInner(output, component, regular, bold, italic);
        return output.build();
    }

    public static RichContent mcToRichContent(List<Component> component) {
        return mcToRichContent(component, null, null, null);
    }

    public static RichContent mcToRichContent(List<Component> component, SizedFont regular, SizedFont bold, SizedFont italic) {
        RichContent.Builder output = new RichContent.Builder();
        for (int i = 0; i < component.size(); i++) {
            if (i != 0)
                output.append("\n");
            mcToRichContentInner(output, component.get(i), regular, bold, italic);
        }
        return output.build();
    }

    public static void mcToRichContentInner(RichContent.Builder output, Component component, SizedFont regular, SizedFont bold, SizedFont italic) {
        component.visit((style, content) -> {
            output.color(style.getColor() != null ? ARGB.setAlpha(style.getColor().getValue(), 255) : 0xFFFFFFFF);
            if (style.isBold() && bold != null) {
                output.font(bold);
            } else if (style.isItalic() && italic != null) {
                output.font(italic);
            } else if (regular != null) {
                output.font(regular);
            } else {
                output.font(GlobalUIState.getDefaultTextFont());
            }
            output.append(content);
            return Optional.empty();
        }, Style.EMPTY);
    }
}
