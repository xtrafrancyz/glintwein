package net.glintwein.fabric.mixin;

import net.glintwein.Glintwein;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    // method_1458 is a lambda in KeyboardHandler.charTyped
    @Inject(method = "method_1458", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z"), cancellable = true, remap = false)
    private static void onCharTyped(GuiEventListener guiEventListener, int codePoint, int modifiers, CallbackInfo ci) {
        if (Glintwein.instance.onCharTyped((char) codePoint, modifiers))
            ci.cancel();
    }

    // second lambda in KeyboardHandler.charTyped
    @Inject(method = "method_1473", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z"), cancellable = true, remap = false)
    private static void onCharTyped2(GuiEventListener guiEventListener, char codePoint, int modifiers, CallbackInfo ci) {
        if (Glintwein.instance.onCharTyped(codePoint, modifiers))
            ci.cancel();
    }
}
