package net.glintwein.mixin.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.glintwein.RenderMatrixTracker;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void captureProjViewMatrix(PoseStack matrixStack, float partialTicks, long finishTimeNano, boolean drawBlockOutline, Camera activeRenderInfo, GameRenderer gameRenderer, LightTexture lightmap, com.mojang.math.Matrix4f projection, CallbackInfo ci) {
        RenderMatrixTracker.update(
            RenderMatrixTracker.toJoml(matrixStack.last().pose()),
            RenderMatrixTracker.toJoml(projection),
            activeRenderInfo.getPosition().x,
            activeRenderInfo.getPosition().y,
            activeRenderInfo.getPosition().z
        );
    }
}
