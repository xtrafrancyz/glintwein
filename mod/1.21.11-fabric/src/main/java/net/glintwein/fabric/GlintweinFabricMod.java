package net.glintwein.fabric;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.glintwein.GlintweinHook;
import net.glintwein.OffscreenHudRenderer;
import net.glintwein.platform.Platform1_21_11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
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
            ScreenMouseEvents.allowMouseClick(screen).register((s, event) -> {
                return !GlintweinHook.onMousePress(event.button());
            });
            ScreenMouseEvents.allowMouseRelease(screen).register((s, event) -> {
                return !GlintweinHook.onMouseRelease(event.button());
            });
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, horizontal, vertical) -> {
                return !GlintweinHook.onMouseScroll((float) horizontal, (float) vertical);
            });
            ScreenKeyboardEvents.allowKeyPress(screen).register((s, event) -> {
                Platform1_21_11.currentKeyEvent = event;
                boolean cancelled = GlintweinHook.onKeyPress(event.key(), event.scancode(), event.modifiers());
                Platform1_21_11.currentKeyEvent = null;
                return !cancelled;
            });
        });
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.TITLE_AND_SUBTITLE,
            Identifier.fromNamespaceAndPath("glintwein", "hud_layer"),
            (context, tickCounter) -> {
                if (!OffscreenHudRenderer.needRender())
                    return;
                Window window = Minecraft.getInstance().getWindow();
                int width = window.getGuiScaledWidth();
                int height = window.getGuiScaledHeight();
                context.blit(
                    RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                    OffscreenHudRenderer.TEXTURE,
                    0, 0, 0, 0, width, height,
                    width, -height,
                    width, height
                );
            }
        );
    }
}