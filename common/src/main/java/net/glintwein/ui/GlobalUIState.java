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
    private static int screenWidth;
    private static int screenHeight;

    public static void init() {
        yogaConfigHandle = Platform.yoga().ConfigNew();
        Platform.yoga().ConfigSetPointScaleFactor(yogaConfigHandle, 1);

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
        return Platform.input().getMouseX() / scale;
    }

    public static float getMouseY() {
        return Platform.input().getMouseY() / scale;
    }

    public static boolean updateUIScale() {
        screenWidth = Platform.get().getScreenWidth();
        screenHeight = Platform.get().getScreenHeight();

        float oldScale = scale;
        scale = Math.min((float) screenWidth / uiResolutionWidth, (float) screenHeight / uiResolutionHeight);
        if (scale != oldScale) {
            Platform.yoga().ConfigSetPointScaleFactor(yogaConfigHandle, scale);
        }
        return scale != oldScale;
    }

    public static float snapToPixel(float value) {
        return Math.round(value * scale) / scale;
    }

    private static final Matrix4f guiProxMatrix = new Matrix4f();
    private static int projMatrixWidth = -1;
    private static int projMatrixHeight = -1;

    public static Matrix4f getGuiProjectionMatrix() {
        int width = Platform.get().getWindowWidth();
        int height = Platform.get().getWindowHeight();
        if (width != projMatrixWidth || height != projMatrixHeight) {
            projMatrixWidth = width;
            projMatrixHeight = height;
            guiProxMatrix.setOrtho2D(0, width, height, 0);
        }
        return guiProxMatrix;
    }
}
