package net.glintwein.fabric.mixin;

import net.glintwein.GlintweinHook;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Inject(method = "charTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;charTyped(Lnet/minecraft/client/input/CharacterEvent;)Z"), cancellable = true, remap = false)
    private void onCharTyped(long handle, CharacterEvent event, CallbackInfo ci) {
        int codePoint = event.codepoint();
        boolean handled = false;
        if (Character.isBmpCodePoint(codePoint)) {
            handled = GlintweinHook.onCharTyped((char) codePoint, 0);
        } else if (Character.isValidCodePoint(codePoint)) {
            handled = GlintweinHook.onCharTyped(Character.highSurrogate(codePoint), 0);
            handled |= GlintweinHook.onCharTyped(Character.lowSurrogate(codePoint), 0);
        }
        if (handled)
            ci.cancel();
    }
}
