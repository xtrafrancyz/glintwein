package net.glintwein.fabric;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.glintwein.GlintweinHook;
import net.glintwein.OffscreenHudRenderer;
import net.glintwein.platform.Platform26_1_2;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class GlintweinFabricMod implements ModInitializer {
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
                Platform26_1_2.currentKeyEvent = event;
                boolean cancelled = GlintweinHook.onKeyPress(event.key(), event.scancode(), event.modifiers());
                Platform26_1_2.currentKeyEvent = null;
                return !cancelled;
            });
        });

        HudElementRegistry.attachElementAfter(
            VanillaHudElements.TITLE_AND_SUBTITLE,
            Identifier.fromNamespaceAndPath("glintwein", "hud_element"),
            (context, tickCounter) -> {
                if (!OffscreenHudRenderer.needRender())
                    return;
                Window window = Minecraft.getInstance().getWindow();
                context.blit(
                    OffscreenHudRenderer.getTextureView(),
                    RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR),
                    0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                    0, 1, 1, 0
                );
            }
        );
    }
}