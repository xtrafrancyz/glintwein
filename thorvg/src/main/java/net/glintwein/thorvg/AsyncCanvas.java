package net.glintwein.thorvg;

import io.github.xtrafrancyz.jthorvg.*;
import net.glintwein.platform.Platform;
import net.glintwein.ui.render.texture.Sprite;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.util.function.BooleanSupplier;

public class AsyncCanvas {
    private final SoftwareCanvas canvas;

    // thorvg thread fields
    private boolean releasedNative;
    private boolean isResizing;
    private boolean initCalled;
    private Runnable onInitCallback;
    private Runnable onResizeCallback;
    private BooleanSupplier onUpdateCallback;
    private Runnable onReleaseCallback;

    // shared fields
    private final Object lock = new Object();
    private IntBuffer pixelBuffer;
    private int width;
    private int height;
    private boolean pixelBufferDirty;

    // opengl thread fields
    private boolean releasedGl;
    private int textureId;
    private int textureWidth;
    private int textureHeight;

    public AsyncCanvas() {
        ThorvgHelper.ensureLoaded();
        this.canvas = Thorvg.newSoftwareCanvas();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Canvas canvas() {
        return canvas;
    }

    public void setOnInitCallback(Runnable callback) {
        this.onInitCallback = callback;
    }

    /**
     * Sets a callback that will be called when the canvas is updated. The callback should return true if the canvas needs to be redrawn, false otherwise.
     */
    public void setOnUpdateCallback(BooleanSupplier callback) {
        this.onUpdateCallback = callback;
    }

    public void setOnResizeCallback(Runnable callback) {
        this.onResizeCallback = callback;
    }

    public void setOnReleaseCallback(Runnable callback) {
        this.onReleaseCallback = callback;
    }

    public void scheduleUpdate() {
        if (releasedNative)
            return;
        ThorvgHelper.execute(() -> updateInner(false));
    }

    public void scheduleResize(int width, int height) {
        if (releasedNative)
            return;
        if (width <= 0 || height <= 0)
            return;
        if (!isResizing && this.width == width && this.height == height)
            return;
        isResizing = true;
        ThorvgHelper.execute(() -> {
            if (this.width == width && this.height == height)
                return;
            resizeInner(width, height);
            isResizing = false;
        });
    }

    private void ensureInitCalled() {
        if (initCalled)
            return;
        initCalled = true;
        if (onInitCallback != null)
            onInitCallback.run();
    }

    private void updateInner(boolean force) {
        if (releasedNative)
            return;

        ensureInitCalled();

        if (onUpdateCallback != null && !onUpdateCallback.getAsBoolean()) {
            if (!force)
                return;
        }

        synchronized (lock) {
            // updates pixelBuffer with the latest canvas content
            canvas.update();
            canvas.draw(false);
            canvas.sync();
            pixelBufferDirty = true;
        }
    }

    private void resizeInner(int width, int height) {
        if (releasedNative)
            return;

        ensureInitCalled();

        int capacity = width * height;

        synchronized (lock) {
            this.width = width;
            this.height = height;
            if (pixelBuffer == null || pixelBuffer.capacity() < capacity) {
                if (pixelBuffer != null) {
                    MemoryUtil.memFree(pixelBuffer);
                    pixelBuffer = null;
                }
                pixelBuffer = MemoryUtil.memAllocInt(capacity);
            }
            canvas.setTarget(SoftwareCanvasTarget.wrap(pixelBuffer, width, height, width, ThorvgColorspace.ABGR8888S));

            if (onResizeCallback != null)
                onResizeCallback.run();

            canvas.update();
            canvas.draw(true);
            canvas.sync();
            pixelBufferDirty = true;
        }

        updateInner(true);
    }

    /**
     * Called from the render thread to get the sprite for rendering.
     */
    public @Nullable Sprite getGlSprite() {
        if (releasedGl)
            throw new IllegalStateException("AsyncCanvas has been closed");

        synchronized (lock) {
            if (pixelBufferDirty && pixelBuffer != null && width > 0 && height > 0) {
                uploadTexture(pixelBuffer, width, height);
                pixelBufferDirty = false;
            } else if (textureId == 0)
                return null;
        }

        return new Sprite(textureId, 0, 0, 1, 1);
    }

    private void uploadTexture(IntBuffer buffer, int w, int h) {
        if (textureId == 0) {
            textureId = GL11.glGenTextures();
            Platform.render().stateBindTexture(textureId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
        } else {
            Platform.render().stateBindTexture(textureId);
        }

        if (w != textureWidth || h != textureHeight) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            textureWidth = w;
            textureHeight = h;
        } else {
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        }
    }

    public void close() {
        if (releasedNative && releasedGl)
            return;
        if (!releasedGl) {
            releasedGl = true;
            releaseGlResources();
        }
        ThorvgHelper.execute(() -> {
            if (releasedNative)
                return;
            releasedNative = true;
            releaseNative();
        });
    }

    private void releaseGlResources() {
        if (textureId != 0) {
            GL11.glDeleteTextures(textureId);
            textureId = 0;
            textureWidth = 0;
            textureHeight = 0;
        }
    }

    private void releaseNative() {
        if (onReleaseCallback != null)
            onReleaseCallback.run();
        canvas.close();
    }
}
