package net.glintwein.ui;

import net.glintwein.platform.Platform;
import net.glintwein.ui.element.Element;

public class GlobalUIState {
    private static Element focusedElement;
    private static Element currentFocus;
    private static boolean captureFocus = false;
    private static boolean focusAliveCheck = false;
    private static boolean focusAlive = false;
    private static long yogaConfigHandle;
    private static float scale = 1;
    private static int screenWidth;
    private static int screenHeight;

    public static void init() {
        yogaConfigHandle = Platform.yoga().ConfigNew();
        Platform.yoga().ConfigSetPointScaleFactor(yogaConfigHandle, 1);
    }

    public static void startFocusCapturing() {
        if (captureFocus)
            throw new IllegalStateException("Focus tracking already started!");
        captureFocus = true;
        currentFocus = focusedElement;
        focusedElement = null;
    }

    public static void stopFocusCapturing() {
        if (!captureFocus)
            throw new IllegalStateException("Focus tracking not started!");
        captureFocus = false;

        if (focusedElement != currentFocus) {
            if (currentFocus != null)
                currentFocus.handleFocusLoss();
            if (focusedElement != null)
                focusedElement.handleFocusGain();
        }
    }

    public static void startFocusAliveCheck() {
        if (focusAliveCheck)
            throw new IllegalStateException("Focus alive check already started!");
        focusAliveCheck = true;
        focusAlive = false;
    }

    public static void stopFocusAliveCheck() {
        if (!focusAliveCheck)
            throw new IllegalStateException("Focus alive check not started!");
        focusAliveCheck = false;
        // no event
        if (!focusAlive)
            focusedElement = null;
    }

    public static Element getFocusedElement() {
        return focusedElement;
    }

    public static void requestFocus(Element element) {
        focusedElement = element;
    }

    public static void clearFocus() {
        if (focusedElement != null)
            focusedElement.handleFocusLoss();
        focusedElement = null;
    }

    public static void tickElement(Element element) {
        if (focusAliveCheck && element == focusedElement)
            focusAlive = true;
    }

    public static long getYogaConfigHandle() {
        return yogaConfigHandle;
    }

    public static int getWindowWidth() {
        return screenWidth;
    }

    public static int getWindowHeight() {
        return screenHeight;
    }

    public static float getScaledWidth() {
        return (float) screenWidth / scale;
    }

    public static float getScaledHeight() {
        return (float) screenHeight / scale;
    }

    public static float getPixelSize() {
        return 1 / scale;
    }

    /**
     * @return 1 или >=1 в зависимости от масштаба, чтобы гарантировать, что линии толщиной в 1 пиксель всегда будут видимыми.
     */
    public static float minimumOnePixel() {
        return scale >= 1 ? 1 : getPixelSize();
    }

    public static float getScale() {
        return scale;
    }

    public static float getMouseX() {
        return Platform.get().getInput().getMouseX() / scale;
    }

    public static float getMouseY() {
        return Platform.get().getInput().getMouseY() / scale;
    }

    public static boolean isMouseGrabbed() {
        return Platform.get().getInput().isMouseGrabbed();
    }

    public static boolean updateUIScale() {
        screenWidth = Platform.get().getScreenWidth();
        screenHeight = Platform.get().getScreenHeight();

        float oldScale = scale;
        scale = Math.min(screenWidth / 1920f, screenHeight / 1080f);
        if (scale != oldScale) {
            Platform.yoga().ConfigSetPointScaleFactor(yogaConfigHandle, scale);
        }
        return scale != oldScale;
    }

    public static float snapToPixel(float value) {
        return Math.round(value * scale) / scale;
    }
}
