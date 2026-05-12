package net.glintwein.ui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.glintwein.ui.render.command.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ContextExt {
    private static final ItemStackRenderState itemState = new ItemStackRenderState();

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
                FeatureRenderDispatcher dispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
                SubmitNodeStorage submitNodeStorage = mc.gameRenderer.getSubmitNodeStorage();
                mc.getItemModelResolver().updateForTopItem(itemState, is, ItemDisplayContext.GUI, mc.level, mc.player, 0);
                Lighting.Entry lighting = itemState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
                mc.gameRenderer.getLighting().setupFor(lighting);
                itemState.submit(pose, submitNodeStorage, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
                dispatcher.renderAllFeatures();
                mc.renderBuffers().bufferSource().endBatch();
            },
            x, y, size, size
        );
    }
}
