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

        // TODO mouse events
    }

    @SubscribeEvent
    public void onPreCharTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
        if (Glintwein.instance.onCharTyped(event.getCodePoint(), event.getModifiers()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onHudRender(RenderGameOverlayEvent.Post event) {
        // TODO fix transparency
        Glintwein.instance.renderHud();
    }
}
