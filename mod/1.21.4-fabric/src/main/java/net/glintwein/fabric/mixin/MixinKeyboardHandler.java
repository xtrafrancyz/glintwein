package net.glintwein.fabric.mixin;

import net.glintwein.GlintweinHook;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Inject(method = "charTyped", at = @At(value = "INVOKE", target = "Ljava/lang/Character;isBmpCodePoint(I)Z"), cancellable = true, remap = false)
    private void onCharTyped(long windowHandle, int codePoint, int modifiers, CallbackInfo ci) {
        boolean handled = false;
        if (Character.isBmpCodePoint(codePoint)) {
            handled = GlintweinHook.onCharTyped((char) codePoint, modifiers);
        } else if (Character.isValidCodePoint(codePoint)) {
            handled = GlintweinHook.onCharTyped(Character.highSurrogate(codePoint), modifiers);
            handled |= GlintweinHook.onCharTyped(Character.lowSurrogate(codePoint), modifiers);
        }
        if (handled)
            ci.cancel();
    }
}
