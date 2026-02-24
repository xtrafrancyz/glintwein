package net.glintwein;

import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.WindowManager;
import net.glintwein.ui.util.NativeCleaner;
import net.glintwein.util.KVStore;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Glintwein {
    public static final Logger LOGGER = LogManager.getLogger();

    public static Glintwein instance;
    private static long timeStart;
    public static long time;

    private final WindowManager windowManager;

    public Glintwein() {
        if (instance != null)
            throw new IllegalStateException("Glintwein instance already exists!");
        instance = this;
        KVStore.load();
        GlobalUIState.init();

        windowManager = new WindowManager();
    }

    public void tick() {
        NativeCleaner.cleanUp();
        KVStore.save();
    }

    public void preRender() {
        updateTime();
        if (timeStart == 0) {
            timeStart = time;
            updateTime();
        }
    }

    public void postRender() {
        GlobalUIState.startFocusAliveCheck();
        windowManager.render();
        GlobalUIState.stopFocusAliveCheck();
    }

    public void updateTime() {
        time = Util.getMillis() - timeStart;
    }

    public boolean onMousePress(Screen s, float mouseX, float mouseY, int button) {
        GlobalUIState.startFocusCapturing();
        boolean cancel = windowManager.onMousePress(
            mouseX * GlintweinFabricMod.getGuiScale(),
            mouseY * GlintweinFabricMod.getGuiScale(),
            button
        );
        GlobalUIState.stopFocusCapturing();
        return cancel;
    }

    public boolean onMouseRelease(Screen s, float mouseX, float mouseY, int button) {
        return windowManager.onMouseRelease(
            mouseX * GlintweinFabricMod.getGuiScale(),
            mouseY * GlintweinFabricMod.getGuiScale(),
            button
        );
    }

    public boolean onMouseScroll(Screen s, float mouseX, float mouseY, float horizontal, float vertical) {
        return windowManager.onMouseScroll(
            mouseX * GlintweinFabricMod.getGuiScale(),
            mouseY * GlintweinFabricMod.getGuiScale(),
            horizontal * GlintweinFabricMod.getGuiScale(),
            vertical * GlintweinFabricMod.getGuiScale()
        );
    }

    public boolean onKeyPress(Screen s, int keyCode, int scanCode, int modifiers) {
        if (GlobalUIState.getFocusedElement() != null) {
            return GlobalUIState.getFocusedElement().handleKeyPress(keyCode, scanCode, modifiers);
        }
        return false;
    }

    public boolean onCharTyped(char character, int keyCode) {
        if (GlobalUIState.getFocusedElement() != null) {
            return GlobalUIState.getFocusedElement().handleCharTyped(character, keyCode);
        }
        return false;
    }
}
