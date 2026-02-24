package net.glintwein.ui;

import net.glintwein.ui.element.Element;
import org.lwjgl.util.yoga.Yoga;

public class GlobalUIState {
    private static Element focusedElement;
    private static Element currentFocus;
    private static boolean captureFocus = false;
    private static boolean focusAliveCheck = false;
    private static boolean focusAlive = false;
    private static long yogaConfigHandle;
    private static float yogaCurrentPixelScale = 1;

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

    public static boolean updateYogaPixelScale(float scale) {
        if (scale != yogaCurrentPixelScale) {
            Yoga.YGConfigSetPointScaleFactor(yogaConfigHandle, scale);
            yogaCurrentPixelScale = scale;
            return true;
        }
        return false;
    }
}
