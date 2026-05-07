package net.glintwein.ui;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawTextureBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class ContextExt {
    public static void drawItem(Context ctx, ItemStack is, float x, float y, float size, boolean decoration) {
        // Item models are not loaded during loading screen, leading to crash
        if (Minecraft.getInstance().overlay instanceof LoadingOverlay)
            return;
        ctx.addPipCommand(
            () -> {
                // default size for items in GUI is 16, so we need to scale it down to 1
                GL11.glScalef(0.0625f, 0.0625f, 0.0625f);
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                itemRenderer.renderGuiItem(is, 0, 0);
                if (decoration) {
                    itemRenderer.renderGuiItemDecorations(Minecraft.getInstance().font, is, 0, 0);
                }
            },
            x, y, size, size
        );
    }

    public static void drawPlayerHead(Context ctx, AbstractClientPlayer player, float x, float y, float size, BorderRadius radius) {
        ResourceLocation skinTextureLocation = player.getSkinTextureLocation();
        int textureId = Minecraft.getInstance().getTextureManager().getTexture(skinTextureLocation).getId();

        // head
        ctx.drawTexture(DrawTextureBuilder.fromXYWH(x, y, size, size)
            .uv(8, 8, 8, 8, 64, 64)
            .texture(textureId)
            .radius(radius)
        );
        // helmet overlay
        ctx.drawTexture(DrawTextureBuilder.fromXYWH(x, y, size, size)
            .uv(40, 8, 8, 8, 64, 64)
            .texture(textureId)
            .radius(radius)
        );
    }
}
