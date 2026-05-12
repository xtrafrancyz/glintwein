package net.glintwein.mixin.ui;

import net.glintwein.GlintweinHook;
import net.glintwein.OffscreenHudRenderer;
import net.glintwein.platform.Platform26_1_2;
import net.glintwein.platform.PlatformProvider;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(CallbackInfo info) {
        PlatformProvider.set(new Platform26_1_2());
        GlintweinHook.init();
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo info) {
        GlintweinHook.tickEnd();
    }

    @Inject(at = @At("HEAD"), method = "runTick(Z)V")
    private void runTickHead(boolean advanceGameTime, CallbackInfo info) {
        GlintweinHook.tickStart();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;update(Lnet/minecraft/client/DeltaTracker;Z)V"), method = "renderFrame(Z)V")
    private void preRender(boolean advanceGameTime, CallbackInfo info) {
        GlintweinHook.preRender();
        OffscreenHudRenderer.preRender();
    }

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen()V",
            shift = At.Shift.AFTER
        ),
        method = "renderFrame(Z)V"
    )
    private void postRender(boolean advanceGameTime, CallbackInfo info) {
        GlintweinHook.postRender();
    }
}
