package net.glintwein;

import net.glintwein.ui.test.TestUI;
import net.glintwein.ui.util.NativeCleaner;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Glintwein {
    public static final Logger LOGGER = LogManager.getLogger();

    public static Glintwein instance;
    private static long timeStart;
    public static long time;

    private TestUI testUI = new TestUI();

    public static void init() {
        instance = new Glintwein();
    }

    public static void tick() {
        NativeCleaner.cleanUp();
    }

    public static void preRender() {
        updateTime();
        if (timeStart == 0) {
            timeStart = time;
            updateTime();
        }

    }

    public static void postRender() {
        instance.testUI.render();
    }

    public static void updateTime() {
        time = Util.getMillis() - timeStart;
    }
}
