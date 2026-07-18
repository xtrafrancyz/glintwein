package net.glintwein;

import net.glintwein.demo.DemoWindow;
import net.glintwein.platform.Platform;
import net.glintwein.platform.ScreenType;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.IngameUILayer;
import net.glintwein.ui.UILayer;
import net.glintwein.ui.render.PipAtlasManager;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.texture.Texture;
import net.glintwein.ui.render.texture.TextureSimple;
import net.glintwein.ui.util.NativeCleaner;
import net.glintwein.util.KVStore;
import net.glintwein.util.PerFrameObjectPool;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Glintwein {
    public static Glintwein instance;
    private static long timeStart;
    public static long time;

    private static final List<Runnable> initListeners = new ArrayList<>();
    private static final List<Runnable> tickStartListeners = new ArrayList<>();
    private static final List<Runnable> tickEndListeners = new ArrayList<>();
    private static final List<Runnable> preRenderListeners = new ArrayList<>();
    private static final List<Runnable> postRenderListeners = new ArrayList<>();

    private final Context sharedDrawContext = new Context();

    private final List<UILayer> uiLayers = new ArrayList<>();
    public final UILayer layerAlwaysOnTop;
    public final UILayer layerIngame;
    private boolean uiTickedThisFrame = false;

    Glintwein() {
        if (instance != null)
            throw new IllegalStateException("Glintwein instance already exists!");
        instance = this;
        KVStore.load();
        GlobalUIState.init();

        layerAlwaysOnTop = new UILayer();
        layerIngame = new IngameUILayer();
        uiLayers.add(layerAlwaysOnTop);
        uiLayers.add(layerIngame);

        if (DemoWindow.isEnabled())
            layerIngame.getWindowManager().addWindow(new DemoWindow());

        runListeners(initListeners, "Init");
        initListeners.clear();
    }

    void tickEnd() {
        PipAtlasManager.reset();
        NativeCleaner.cleanUp();
        KVStore.save();
        PerFrameObjectPool.onFrameEndAllPools();
        runListeners(tickEndListeners, "Tick end");
    }

    void tickStart() {
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
        runListeners(tickStartListeners, "Tick start");
    }

    void preRender() {
        uiTickedThisFrame = false;
        runListeners(preRenderListeners, "Pre render");
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

    void renderLayerIngame() {
        tickUIIfNeeded();
        layerIngame.render(sharedDrawContext);
    }

    void postRender() {
        tickUIIfNeeded();
        layerAlwaysOnTop.render(sharedDrawContext);
        runListeners(postRenderListeners, "Post render");
    }

    private void updateTime() {
        time = Platform.get().getTimeMillis() - timeStart;
    }

    boolean onMousePress(int button) {
        GlobalUIState.startFocusCapturing();
        boolean cancel = false;
        try {
            // use captured mouse coords to prevent issues with scaling
            float mouseX = GlobalUIState.getMouseX();
            float mouseY = GlobalUIState.getMouseY();
            for (UILayer layer : uiLayers) {
                if (layer.onMousePress(mouseX, mouseY, button)) {
                    cancel = true;
                    break;
                }
            }
        } catch (Exception e) {
            Platform.log().error("Exception while handling mouse press", e);
        }
        GlobalUIState.stopFocusCapturing();
        return cancel;
    }

    boolean onMouseRelease(int button) {
        // use captured mouse coords to prevent issues with scaling
        float mouseX = GlobalUIState.getMouseX();
        float mouseY = GlobalUIState.getMouseY();
        boolean cancel = false;
        try {
            for (UILayer layer : uiLayers) {
                if (layer.onMouseRelease(mouseX, mouseY, button)) {
                    cancel = true;
                    break;
                }
            }
        } catch (Exception e) {
            Platform.log().error("Exception while handling mouse release", e);
        }
        return cancel;
    }

    boolean onMouseScroll(float horizontal, float vertical) {
        // use captured mouse coords to prevent issues with scaling
        float mouseX = GlobalUIState.getMouseX();
        float mouseY = GlobalUIState.getMouseY();
        boolean cancel = false;
        try {
            for (UILayer layer : uiLayers) {
                if (layer.onMouseScroll(mouseX, mouseY, horizontal, vertical)) {
                    cancel = true;
                    break;
                }
            }
        } catch (Exception e) {
            Platform.log().error("Exception while handling mouse scroll", e);
        }
        return cancel;
    }

    boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (GlobalUIState.getFocusedElement() != null) {
            try {
                return GlobalUIState.getFocusedElement().handleKeyPress(keyCode, scanCode, modifiers);
            } catch (Exception e) {
                Platform.log().error("Exception while handling key press", e);
                return false;
            }
        }
        return false;
    }

    boolean onCharTyped(char character, int modifiers) {
        if (GlobalUIState.getFocusedElement() != null) {
            try {
                return GlobalUIState.getFocusedElement().handleCharTyped(character, modifiers);
            } catch (Exception e) {
                Platform.log().error("Exception while handling char typed", e);
                return false;
            }
        }
        return false;
    }

    public static void addInitListener(Runnable listener) {
        if (instance == null) {
            initListeners.add(listener);
        } else {
            listener.run();
        }
    }

    public static void addTickStartListener(Runnable listener) {
        tickStartListeners.add(listener);
    }

    public static void addTickEndListener(Runnable listener) {
        tickEndListeners.add(listener);
    }

    public static void addPreRenderListener(Runnable listener) {
        preRenderListeners.add(listener);
    }

    public static void addPostRenderListener(Runnable listener) {
        postRenderListeners.add(listener);
    }

    private static void runListeners(List<Runnable> listeners, String listenerType) {
        if (listeners.isEmpty())
            return;
        for (Runnable listener : listeners) {
            try {
                listener.run();
            } catch (Exception e) {
                Platform.log().error(listenerType + " listener error", e);
            }
        }
    }

    public static GigaFont loadFont(InputStream json, InputStream texture) {
        return GigaFont.load(json, texture);
    }

    public static GigaFont loadFont(String name, InputStream json, InputStream texture) {
        return GigaFont.load(name, json, texture);
    }

    public static Texture loadTexture(InputStream is) {
        try {
            return new TextureSimple(Platform.get().loadImage(is));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load texture", e);
        }
    }
}
