package net.glintwein.ui;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawTextureBuilder;
import net.glintwein.ui.render.command.PipCommand;
import net.glintwein.ui.render.texture.AtlasPacker;
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
        addNativePipCommand(ctx, () -> {
            // default size for items in GUI is 16, so we need to scale it down to 1
            GL11.glScalef(0.0625f, 0.0625f, 0.0625f);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            itemRenderer.renderGuiItem(is, 0, 0);
            if (decoration) {
                itemRenderer.renderGuiItemDecorations(Minecraft.getInstance().font, is, 0, 0);
            }
        }, x, y, size, size);
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

    public static void addNativePipCommand(Context ctx, Runnable command, float x, float y, float width, float height) {
        ctx.addPipCommand(cmd -> {
            GL11.glPushMatrix();
            glTransformToPipRect(cmd);
            command.run();
            GL11.glPopMatrix();
        }, x, y, width, height);
    }

    private static void glTransformToPipRect(PipCommand cmd) {
        AtlasPacker.Rect rect = cmd.sprite.atlasRect();
        GL11.glTranslatef(rect.left, rect.top, -3000);
        float sx = rect.right - rect.left;
        float sy = rect.bottom - rect.top;
        GL11.glScalef(sx, sy, (sx + sy) / 2f);
    }
}
