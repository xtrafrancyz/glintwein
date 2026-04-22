package net.glintwein;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("glintwein")
public class GlintweinForgeMod {
    private static final Logger LOGGER = LogManager.getLogger();

    public GlintweinForgeMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPreCharTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
        if (Glintwein.instance.onCharTyped(event.getCodePoint(), event.getModifiers()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPreKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        if (Glintwein.instance.onKeyPress(event.getKeyCode(), event.getScanCode(), event.getModifiers()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPreMouseClicked(GuiScreenEvent.MouseClickedEvent.Pre event) {
        if (Glintwein.instance.onMousePress((float) event.getMouseX(), (float) event.getMouseY(), event.getButton()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPreMouseReleased(GuiScreenEvent.MouseReleasedEvent.Pre event) {
        if (Glintwein.instance.onMouseRelease((float) event.getMouseX(), (float) event.getMouseY(), event.getButton()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPreMouseScrolled(GuiScreenEvent.MouseScrollEvent.Pre event) {
        if (Glintwein.instance.onMouseScroll((float) event.getMouseX(), (float) event.getMouseY(), 0, (float) event.getScrollDelta()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onHudRender(RenderGameOverlayEvent.Chat event) {
        Glintwein.instance.renderHud();
    }
}
