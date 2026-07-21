package net.glintwein.impl;

import net.glintwein.ui.MinecraftTooltipRenderer;
import net.glintwein.ui.element.Element;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class GlintTooltipComponent implements ClientTooltipComponent {
    private final Element el;

    public GlintTooltipComponent(Element el) {
        this.el = el;
    }

    @Override
    public int getHeight(Font font) {
        return (int) Math.ceil(el.getComputedHeight());
    }

    @Override
    public int getWidth(Font font) {
        return (int) Math.ceil(el.getComputedWidth());
    }

    @Override
    public void renderText(GuiGraphics guiGraphics, Font font, int x, int y) {
        MinecraftTooltipRenderer.setElementPos(el, x, y);
    }
}
