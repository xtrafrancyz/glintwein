package net.glintwein;

import net.glintwein.ui.WindowManager;
import net.glintwein.ui.util.NativeCleaner;
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

        windowManager = new WindowManager();
    }

    public void tick() {
        NativeCleaner.cleanUp();
    }

    public void preRender() {
        updateTime();
        if (timeStart == 0) {
            timeStart = time;
            updateTime();
        }
    }

    public void postRender() {
        windowManager.render();
    }

    public void updateTime() {
        time = Util.getMillis() - timeStart;
    }

    public boolean onMousePress(Screen s, float mouseX, float mouseY, int button) {
        return windowManager.onMousePress(mouseX, mouseY, button);
    }

    public boolean onMouseRelease(Screen s, float mouseX, float mouseY, int button) {
        return windowManager.onMouseRelease(mouseX, mouseY, button);
    }

    public boolean onMouseScroll(Screen s, float mouseX, float mouseY, float amount, float vertical) {
        return windowManager.onMouseScroll(mouseX, mouseY, amount, vertical);
    }
}
