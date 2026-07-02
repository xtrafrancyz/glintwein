package net.glintwein.ui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawTextureBuilder;
import net.glintwein.ui.render.texture.AtlasPacker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class ContextExt {
    private static final ItemStackRenderState tempItemStackRenderState = new ItemStackRenderState();

    public static void drawItem(Context ctx, ItemStack is, float x, float y, float size, boolean decoration) {
        // Item models are not loaded during loading screen, leading to crash
        if (is.isEmpty() || Minecraft.getInstance().getOverlay() instanceof LoadingOverlay)
            return;
        addNativePipCommand(ctx, pose -> {
            pose.translate(0.5f, 0.5f, 1);
            pose.scale(1, -1, 1);

            Minecraft mc = Minecraft.getInstance();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            mc.getItemModelResolver().updateForTopItem(tempItemStackRenderState, is, ItemDisplayContext.GUI, false, mc.level, mc.player, 0);
            boolean bl = !tempItemStackRenderState.usesBlockLight();
            if (bl) {
                bufferSource.endBatch();
                Lighting.setupForFlatItems();
            }

            tempItemStackRenderState.render(pose, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            bufferSource.endBatch();
            if (bl) {
                Lighting.setupFor3DItems();
            }
        }, x, y, size, size);
    }

    public static void drawPlayerHead(Context ctx, AbstractClientPlayer player, float x, float y, float size, BorderRadius radius) {
        ResourceLocation skinTextureLocation = player.getSkin().texture();
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

    public static void addNativePipCommand(Context ctx, Consumer<PoseStack> command, float x, float y, float width, float height) {
        ctx.addPipCommand(cmd -> {
            PoseStack pose = new PoseStack();

            AtlasPacker.Rect rect = cmd.sprite.atlasRect();
            pose.translate(rect.left, rect.top, -3000);
            float sx = rect.right - rect.left;
            float sy = rect.bottom - rect.top;
            pose.scale(sx, sy, (sx + sy) / 2f);

            command.accept(pose);
        }, x, y, width, height);
    }
}
