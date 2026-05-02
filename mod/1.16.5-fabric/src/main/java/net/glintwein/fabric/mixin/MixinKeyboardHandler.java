package net.glintwein.fabric.mixin;

import net.glintwein.Glintwein;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    // second lambda in KeyboardHandler.charTyped
    @Inject(method = "charTyped", at = @At(value = "INVOKE", target = "Ljava/lang/Character;charCount(I)I"), cancellable = true, remap = false)
    private void onCharTyped(long windowPointer, int codePoint, int modifiers, CallbackInfo ci) {
        boolean handled = false;
        if (Character.isBmpCodePoint(codePoint)) {
            handled = Glintwein.instance.onCharTyped((char) codePoint, modifiers);
        } else if (Character.isValidCodePoint(codePoint)) {
            handled = Glintwein.instance.onCharTyped(Character.highSurrogate(codePoint), modifiers);
            handled |= Glintwein.instance.onCharTyped(Character.lowSurrogate(codePoint), modifiers);
        }
        if (handled)
            ci.cancel();
    }
}
