package net.glintwein.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.glintwein.ui.render.command.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public class ContextExt {
    public static void drawItem(Context ctx, ItemStack is, float x, float y, float size, boolean decoration) {
        // Item models are not loaded during loading screen, leading to crash
        if (Minecraft.getInstance().overlay instanceof LoadingOverlay)
            return;
        ctx.addPipCommand(
            () -> {
                // default size for items in GUI is 16, so we need to scale it down to 1
                GlStateManager._scalef(0.0625f, 0.0625f, 0.0625f);
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                itemRenderer.renderGuiItem(is, 0, 0);
                if (decoration) {
                    itemRenderer.renderGuiItemDecorations(Minecraft.getInstance().font, is, 0, 0);
                }
            },
            x, y, size, size
        );
    }
}
