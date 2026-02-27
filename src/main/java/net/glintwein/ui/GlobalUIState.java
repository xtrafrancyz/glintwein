package net.glintwein.ui;

import net.glintwein.ui.element.Element;
import net.minecraft.client.Minecraft;
import org.lwjgl.util.yoga.Yoga;

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
        yogaConfigHandle = Yoga.YGConfigNew();
        Yoga.YGConfigSetPointScaleFactor(yogaConfigHandle, 1);
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

    public static float getScale() {
        return scale;
    }

    public static float getMouseX() {
        Minecraft mc = Minecraft.getInstance();
        return (float) mc.mouseHandler.xpos() / scale;
    }

    public static float getMouseY() {
        Minecraft mc = Minecraft.getInstance();
        return (float) mc.mouseHandler.ypos() / scale;
    }

    public static boolean isMouseGrabbed() {
        Minecraft mc = Minecraft.getInstance();
        return mc.mouseHandler.isMouseGrabbed();
    }

    public static boolean updateUIScale() {
        Minecraft mc = Minecraft.getInstance();
        screenWidth = mc.getWindow().getWidth();
        screenHeight = mc.getWindow().getHeight();

        float oldScale = scale;
        scale = Math.min(screenWidth / 1920f, screenHeight / 1080f);
        if (scale != oldScale) {
            Yoga.YGConfigSetPointScaleFactor(yogaConfigHandle, scale);
        }
        return scale != oldScale;
    }

    public static float snapToPixel(float value) {
        return Math.round(value * scale) / scale;
    }
}
