package net.glintwein.mixin.ui;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.glintwein.platform.PlatformRender1_21_4;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void captureProjViewMatrix(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, Matrix4f viewMatrix, Matrix4f proxMatrix, CallbackInfo ci) {
        PlatformRender1_21_4.projMatrix.set(proxMatrix);
        PlatformRender1_21_4.viewMatrix.set(viewMatrix);
        PlatformRender1_21_4.cameraPos.set(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
    }
}
