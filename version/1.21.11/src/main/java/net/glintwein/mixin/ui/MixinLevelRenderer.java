package net.glintwein.mixin.ui;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.glintwein.platform.PlatformRender1_21_11;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void captureProjViewMatrix(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, Matrix4f frustumMatrix, Matrix4f projectionMatrix, Matrix4f cullingProjectionMatrix, GpuBufferSlice shaderFog, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        PlatformRender1_21_11.projMatrix.set(projectionMatrix);
        PlatformRender1_21_11.viewMatrix.set(frustumMatrix);
        PlatformRender1_21_11.cameraPos.set(camera.position().x, camera.position().y, camera.position().z);
    }
}
