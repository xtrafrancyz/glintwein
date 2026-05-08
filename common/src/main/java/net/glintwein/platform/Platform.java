package net.glintwein.platform;

import net.glintwein.ui.render.command.PipCommand;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.util.PriorityQueue;

public interface Platform {
    static Platform get() {
        return PlatformProvider.get();
    }

    static YogaShim yoga() {
        return get().getYogaShim();
    }

    static Render render() {
        return get().getRender();
    }

    static Input input() {
        return get().getInput();
    }

    long getTimeMillis();

    float getGuiScale();

    int getScreenWidth();

    int getScreenHeight();

    int getWindowWidth();

    int getWindowHeight();

    ScreenType getScreenType();

    GlintImage loadImage(InputStream is) throws IOException;

    Render getRender();

    YogaShim getYogaShim();

    Input getInput();

    interface Input {
        float getMouseX();

        float getMouseY();

        boolean isMouseGrabbed();

        boolean hasControlDown();

        boolean hasShiftDown();

        boolean hasAltDown();

        void setClipboard(String text);

        String getClipboard();
    }

    interface Render {
        Vector3f getCameraPos();

        Matrix4f getWorldProjMatrix();

        Matrix4f getWorldViewMatrix();

        void stateActiveTexture(int texture);

        void stateBindTexture(int texture);

        void stateEnableScissor(int x, int y, int width, int height);

        void stateDisableScissor();

        void renderPipList(PriorityQueue<PipCommand> commands);

        void beforeDraw();

        void afterDraw();

        GlintRenderTarget createRenderTarget(int width, int height, boolean useDepth);

        default boolean shouldUseVAO() {
            return false;
        }

        default AutoQuadIndexBuffer getQuadAutoIndexBuffer() {
            throw new UnsupportedOperationException("AutoQuadIndexBuffer is not supported on this platform");
        }
    }
}
