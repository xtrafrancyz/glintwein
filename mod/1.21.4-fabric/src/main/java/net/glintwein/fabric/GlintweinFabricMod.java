package net.glintwein.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.glintwein.GlintweinHook;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlintweinFabricMod implements ModInitializer {
    public static final String MOD_ID = "glintwein";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
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
        HudLayerRegistrationCallback.EVENT.register(
            layeredDrawer -> layeredDrawer.attachLayerAfter(
                IdentifiedLayer.TITLE_AND_SUBTITLE,
                ResourceLocation.fromNamespaceAndPath("glintwein", "hud_layer"),
                (context, tickCounter) -> {
                    context.flush();
                    GlintweinHook.renderLayerIngame();
                }
            )
        );
    }
}