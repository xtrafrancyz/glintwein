package net.glintwein.mixin.ui;

import net.glintwein.GlintweinHook;
import net.glintwein.OffscreenHudRenderer;
import net.glintwein.demo.DemoWindow;
import net.glintwein.platform.Platform1_21_11;
import net.glintwein.platform.PlatformProvider;
import net.glintwein.impl.DemoSection1_21_11;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(CallbackInfo info) {
        PlatformProvider.set(new Platform1_21_11());
        GlintweinHook.init();
        DemoWindow.addCustomInitializer(demo -> {
            demo.root.addChild(new DemoWindow.Collapse("1.21.11 Specific", new DemoSection1_21_11()));
        });
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo info) {
        GlintweinHook.tickEnd();
    }

    @Inject(at = @At("HEAD"), method = "runTick(Z)V")
    private void runTickHead(boolean renderLevel, CallbackInfo info) {
        GlintweinHook.tickStart();
    }

    @Inject(
        method = "runTick(Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"
        )
    )
    private void runTickBeforeRender(boolean renderLevel, CallbackInfo info) {
        GlintweinHook.preRender();
        OffscreenHudRenderer.preRender();
    }

    @Inject(
        method = "runTick(Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen()V",
            shift = At.Shift.AFTER
        )
    )
    private void runTickAfterRender(boolean renderLevel, CallbackInfo info) {
        GlintweinHook.postRender();
    }
}
