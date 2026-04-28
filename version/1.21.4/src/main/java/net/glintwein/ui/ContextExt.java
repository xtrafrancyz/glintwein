package net.glintwein.ui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.glintwein.ui.render.command.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ContextExt {
    private static final ItemStackRenderState tempItemStackRenderState = new ItemStackRenderState();

    // Заменяется на новый PoseStack при каждом рендере, так что не должно быть проблем с состоянием
    public static PoseStack pose = new PoseStack();

    public static void drawItem(Context ctx, ItemStack is, float x, float y, float size, boolean decoration) {
        // Item models are not loaded during loading screen, leading to crash
        if (is.isEmpty() || Minecraft.getInstance().getOverlay() instanceof LoadingOverlay)
            return;
        ctx.addPipCommand(
            () -> {
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
            },
            x, y, size, size
        );
    }
}
