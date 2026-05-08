package net.glintwein;

import net.glintwein.platform.PlatformProvider;

public class GlintweinHook {
    public static void init() {
        if (PlatformProvider.get() == null)
            throw new IllegalStateException("Platform not set");
        new Glintwein();
    }

    public static void tickStart() {
        Glintwein.instance.tickStart();
    }

    public static void tickEnd() {
        Glintwein.instance.tickEnd();
    }

    public static void preRender() {
        Glintwein.instance.preRender();
    }

    public static void postRender() {
        Glintwein.instance.postRender();
    }

    public static void renderLayerIngame() {
        Glintwein.instance.renderLayerIngame();
    }

    public static boolean onMousePress(int button) {
        return Glintwein.instance.onMousePress(button);
    }

    public static boolean onMouseRelease(int button) {
        return Glintwein.instance.onMouseRelease(button);
    }

    public static boolean onMouseScroll(float horizontal, float vertical) {
        return Glintwein.instance.onMouseScroll(horizontal, vertical);
    }

    public static boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        return Glintwein.instance.onKeyPress(keyCode, scanCode, modifiers);
    }

    public static boolean onCharTyped(char character, int modifiers) {
        return Glintwein.instance.onCharTyped(character, modifiers);
    }
}
