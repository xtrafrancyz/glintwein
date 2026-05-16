package net.glintwein.ui;

import net.glintwein.Glintwein;
import net.glintwein.platform.Platform;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.font.SizedFont;
import net.glintwein.util.ResourceLoaderUtil;
import org.joml.Matrix4f;

import java.io.InputStream;

public class GlobalUIState {
    private static GigaFont defaultFont;
    private static SizedFont defaultTextFont;
    private static int uiResolutionWidth = 1920;
    private static int uiResolutionHeight = 1080;
    private static Element focusedElement;
    private static Element currentFocus;
    private static boolean captureFocus = false;
    private static boolean focusAliveCheck = false;
    private static boolean focusAlive = false;
    private static long yogaConfigHandle;
    private static float scale = 1;
    private static final Matrix4f guiProjMatrix = new Matrix4f();
    private static int lastWindowWidth = -1;
    private static int lastWindowHeight = -1;

    public static void init() {
        yogaConfigHandle = Platform.yoga().ConfigNew();
        // disable automatic scaling, we will handle it ourselves
        Platform.yoga().ConfigSetPointScaleFactor(yogaConfigHandle, 0);

        String defaultFontPath = "assets/fonts/sf-pro-regular";
        try (InputStream jsonStream = ResourceLoaderUtil.getStream(defaultFontPath + ".json");
             InputStream imageStream = ResourceLoaderUtil.getStream(defaultFontPath + ".png")) {
            defaultFont = Glintwein.loadFont(jsonStream, imageStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load GigaFont: " + defaultFontPath, e);
        }

        defaultTextFont = new SizedFont(defaultFont, 16);
    }

    public static GigaFont getDefaultFont() {
        return defaultFont;
    }

    public static SizedFont getDefaultTextFont() {
        return defaultTextFont;
    }

    public static void setDefaultTextFont(SizedFont font) {
        defaultTextFont = font;
    }

    public static void setUiResolution(int width, int height) {
        uiResolutionWidth = width;
        uiResolutionHeight = height;
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
            clearCurrentFocus();
    }

    public static void clearCurrentFocus() {
        if (focusedElement != null)
            focusedElement.handleFocusLoss();
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

    public static float getScaledWidth() {
        return lastWindowWidth / scale;
    }

    public static float getScaledHeight() {
        return lastWindowHeight / scale;
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
        return Platform.input().getMouseX() / scale;
    }

    public static float getMouseY() {
        return Platform.input().getMouseY() / scale;
    }

    public static boolean updateUIScale() {
        int width = Platform.get().getWindowWidth();
        int height = Platform.get().getWindowHeight();
        if (lastWindowHeight == height && lastWindowWidth == width)
            return false;
        lastWindowWidth = width;
        lastWindowHeight = height;

        guiProjMatrix.setOrtho2D(0, width, height, 0);

        scale = Math.min((float) width / uiResolutionWidth, (float) height / uiResolutionHeight);
        //Platform.yoga().ConfigSetPointScaleFactor(yogaConfigHandle, scale);
        return true;
    }

    public static float snapToPixel(float value) {
        return (float) (Math.floor(value * scale + 0.5) / scale);
    }

    public static float floorToPixel(float value) {
        return (float) (Math.floor(value * scale) / scale);
    }

    public static Matrix4f getGuiProjectionMatrix() {
        return guiProjMatrix;
    }
}
