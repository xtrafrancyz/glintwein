package net.glintwein.mixin.ui;

import net.glintwein.Glintwein;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(CallbackInfo info) {
        new Glintwein();
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo info) {
        Glintwein.instance.tick();
    }

    @Inject(at = @At("HEAD"), method = "runTick(Z)V")
    private void runTickHead(boolean renderLevel, CallbackInfo info) {
        Glintwein.instance.preRender();
    }

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V",
            shift = At.Shift.AFTER
        ),
        method = "runTick(Z)V"
    )
    private void runTickAfterRender(boolean renderLevel, CallbackInfo info) {
        Glintwein.instance.postRender();
    }
}
