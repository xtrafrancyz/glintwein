package net.glintwein.mixin.ui;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.glintwein.platform.PlatformRender26_1_2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
    @Inject(method = "_glBindFramebuffer", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onBindFramebuffer(int target, int framebuffer, CallbackInfo ci) {
        if (PlatformRender26_1_2.isFrameBufferLocked)
            ci.cancel();
    }
}
