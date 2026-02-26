package net.glintwein;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;

public class GlintweinFabricMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> {
                return !Glintwein.instance.onMousePress(s, (float) mouseX, (float) mouseY, button);
            });
            ScreenMouseEvents.allowMouseRelease(screen).register((s, mouseX, mouseY, button) -> {
                return !Glintwein.instance.onMouseRelease(s, (float) mouseX, (float) mouseY, button);
            });
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, horizontal, vertical) -> {
                return !Glintwein.instance.onMouseScroll(s, (float) mouseX, (float) mouseY, (float) horizontal, (float) vertical);
            });
            ScreenKeyboardEvents.allowKeyPress(screen).register((s, keyCode, scanCode, modifiers) -> {
                return !Glintwein.instance.onKeyPress(s, keyCode, scanCode, modifiers);
            });
        });
        HudRenderCallback.EVENT.register((pose, tickDelta) -> {
            Glintwein.instance.renderHud();
        });
    }

    public static float getGuiScale() {
        return (float) Minecraft.getInstance().getWindow().getGuiScale();
    }
}
