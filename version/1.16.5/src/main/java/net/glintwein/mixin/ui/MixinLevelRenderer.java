package net.glintwein.mixin.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.glintwein.platform.PlatformRender1_16_5;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Unique
    private static final FloatBuffer glintwein$matrixCopyBuffer = ByteBuffer.allocateDirect(16 * 4).asFloatBuffer();

    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void captureProjViewMatrix(PoseStack matrixStack, float partialTicks, long finishTimeNano, boolean drawBlockOutline, Camera activeRenderInfo, GameRenderer gameRenderer, LightTexture lightmap, com.mojang.math.Matrix4f projection, CallbackInfo ci) {
        glintwein$copyMatrix(projection, PlatformRender1_16_5.projMatrix);
        glintwein$copyMatrix(matrixStack.last().pose(), PlatformRender1_16_5.viewMatrix);
    }

    @Unique
    private static void glintwein$copyMatrix(com.mojang.math.Matrix4f source, Matrix4f dest) {
        glintwein$matrixCopyBuffer.clear();
        source.store(glintwein$matrixCopyBuffer);
        glintwein$matrixCopyBuffer.rewind();
        dest.set(glintwein$matrixCopyBuffer);
    }
}
