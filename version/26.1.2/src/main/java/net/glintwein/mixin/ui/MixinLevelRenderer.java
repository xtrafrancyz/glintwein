package net.glintwein.mixin.ui;

import net.glintwein.platform.PlatformRender26_1_2;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "extractLevel", at = @At(value = "TAIL"))
    private void captureCamera(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo ci) {
        camera.getViewRotationMatrix(PlatformRender26_1_2.viewMatrix);
        camera.getViewRotationProjectionMatrix(PlatformRender26_1_2.projMatrix);
    }
}
