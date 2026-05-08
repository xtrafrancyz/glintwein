package net.glintwein.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.glintwein.GlintweinHook;

public class GlintweinFabricMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> {
                return !GlintweinHook.onMousePress(button);
            });
            ScreenMouseEvents.allowMouseRelease(screen).register((s, mouseX, mouseY, button) -> {
                return !GlintweinHook.onMouseRelease(button);
            });
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, horizontal, vertical) -> {
                return !GlintweinHook.onMouseScroll((float) horizontal, (float) vertical);
            });
            ScreenKeyboardEvents.allowKeyPress(screen).register((s, keyCode, scanCode, modifiers) -> {
                return !GlintweinHook.onKeyPress(keyCode, scanCode, modifiers);
            });
        });
        HudRenderCallback.EVENT.register((pose, tickDelta) -> {
            GlintweinHook.renderLayerIngame();
        });
    }
}
