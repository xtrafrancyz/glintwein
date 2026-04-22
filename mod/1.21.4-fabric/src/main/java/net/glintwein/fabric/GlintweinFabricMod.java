package net.glintwein.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.glintwein.Glintwein;
import net.glintwein.ui.ContextExt;
import net.glintwein.ui.render.command.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                return !Glintwein.instance.onMousePress((float) mouseX, (float) mouseY, button);
            });
            ScreenMouseEvents.allowMouseRelease(screen).register((s, mouseX, mouseY, button) -> {
                return !Glintwein.instance.onMouseRelease((float) mouseX, (float) mouseY, button);
            });
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, horizontal, vertical) -> {
                return !Glintwein.instance.onMouseScroll((float) mouseX, (float) mouseY, (float) horizontal, (float) vertical);
            });
            ScreenKeyboardEvents.allowKeyPress(screen).register((s, keyCode, scanCode, modifiers) -> {
                return !Glintwein.instance.onKeyPress(keyCode, scanCode, modifiers);
            });
        });
        HudLayerRegistrationCallback.EVENT.register(
            layeredDrawer -> layeredDrawer.attachLayerAfter(
                IdentifiedLayer.TITLE_AND_SUBTITLE,
                ResourceLocation.fromNamespaceAndPath("glintwein", "hud_layer"),
                (context, tickCounter) -> {
                    context.flush();
                    Glintwein.instance.renderHud();

                    Context ctx = new Context();
                    ctx.drawRect(0, 0, 300, 300, 0xffffffff);
                    ContextExt.drawItem(ctx, new ItemStack(Items.APPLE), 0, 0, 300, false);
                    ctx.execute();
                }
            )
        );
    }
}