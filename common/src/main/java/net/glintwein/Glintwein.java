package net.glintwein;

import net.glintwein.devtest.DevTest;
import net.glintwein.platform.Platform;
import net.glintwein.platform.ScreenType;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.IngameUILayer;
import net.glintwein.ui.UILayer;
import net.glintwein.ui.render.PipAtlasManager;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.util.NativeCleaner;
import net.glintwein.util.KVStore;

import java.util.ArrayList;
import java.util.List;

public class Glintwein {
    public static Glintwein instance;
    private static long timeStart;
    public static long time;

    public static final Context sharedDrawContext = new Context();

    private final List<UILayer> uiLayers = new ArrayList<>();
    public final UILayer layerAlwaysOnTop;
    public final UILayer layerIngame;
    private boolean uiTickedThisFrame = false;

    public Glintwein() {
        if (instance != null)
            throw new IllegalStateException("Glintwein instance already exists!");
        instance = this;
        KVStore.load();
        GlobalUIState.init();

        layerAlwaysOnTop = new UILayer();
        layerIngame = new IngameUILayer();
        uiLayers.add(layerAlwaysOnTop);
        uiLayers.add(layerIngame);

        if (Boolean.getBoolean("glintwein.devtest"))
            DevTest.init();
    }

    public void tick() {
        NativeCleaner.cleanUp();
        KVStore.save();
    }

    public void tickStart() {
        updateTime();
        if (timeStart == 0) {
            timeStart = time;
            updateTime();
        }

        if (GlobalUIState.updateUIScale()) {
            for (UILayer layer : uiLayers) {
                layer.invalidateLayout();
            }
        }
        PipAtlasManager.tickStart();
    }

    public void preRender() {
        uiTickedThisFrame = false;
    }

    private void tickUIIfNeeded() {
        if (!uiTickedThisFrame) {
            GlobalUIState.startFocusAliveCheck();
            for (UILayer layer : uiLayers)
                layer.tick();
            GlobalUIState.stopFocusAliveCheck();
            if (Platform.get().getScreenType() == ScreenType.NONE)
                GlobalUIState.clearCurrentFocus();
            uiTickedThisFrame = true;
        }
    }

    public void renderHud() {
        tickUIIfNeeded();
        layerIngame.render();
    }

    public void postRender() {
        tickUIIfNeeded();
        layerAlwaysOnTop.render();
    }

    public void updateTime() {
        time = Platform.get().getTimeMillis() - timeStart;
    }

    public boolean onMousePress(float mouseX, float mouseY, int button) {
        GlobalUIState.startFocusCapturing();
        mouseX = scaleMouseCoord(mouseX);
        mouseY = scaleMouseCoord(mouseY);
        boolean cancel = false;
        for (UILayer layer : uiLayers) {
            if (layer.onMousePress(mouseX, mouseY, button)) {
                cancel = true;
                break;
            }
        }
        GlobalUIState.stopFocusCapturing();
        return cancel;
    }

    public boolean onMouseRelease(float mouseX, float mouseY, int button) {
        mouseX = scaleMouseCoord(mouseX);
        mouseY = scaleMouseCoord(mouseY);
        boolean cancel = false;
        for (UILayer layer : uiLayers) {
            if (layer.onMouseRelease(mouseX, mouseY, button)) {
                cancel = true;
                break;
            }
        }
        return cancel;
    }

    public boolean onMouseScroll(float mouseX, float mouseY, float horizontal, float vertical) {
        mouseX = scaleMouseCoord(mouseX);
        mouseY = scaleMouseCoord(mouseY);
        boolean cancel = false;
        for (UILayer layer : uiLayers) {
            if (layer.onMouseScroll(mouseX, mouseY, horizontal, vertical)) {
                cancel = true;
                break;
            }
        }
        return cancel;
    }

    private static float scaleMouseCoord(float coord) {
        return coord * Platform.get().getGuiScale() / GlobalUIState.getScale();
    }

    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (GlobalUIState.getFocusedElement() != null) {
            return GlobalUIState.getFocusedElement().handleKeyPress(keyCode, scanCode, modifiers);
        }
        return false;
    }

    public boolean onCharTyped(char character, int modifiers) {
        if (GlobalUIState.getFocusedElement() != null) {
            return GlobalUIState.getFocusedElement().handleCharTyped(character, modifiers);
        }
        return false;
    }
}
