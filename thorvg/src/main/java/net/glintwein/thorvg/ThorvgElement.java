package net.glintwein.thorvg;

import net.glintwein.ui.element.LeafElement;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.texture.Sprite;
import net.glintwein.ui.util.GMath;
import net.glintwein.ui.util.NativeCleaner;
import org.joml.Vector2f;

import java.lang.ref.WeakReference;

public abstract class ThorvgElement extends LeafElement {
    private final AsyncCanvas canvas;
    private Content content;

    private float ownWidth;
    private float ownHeight;

    public ThorvgElement() {
        canvas = new AsyncCanvas();

        setMeasureFunctionAspectRatio(() -> ownWidth, () -> ownHeight);

        WeakReference<ThorvgElement> weakThis = new WeakReference<>(this);
        NativeCleaner.register(this, () -> {
            ThorvgElement element = weakThis.get();
            if (element == null)
                return;
            element.releaseNative();
        });
    }

    protected void setContentSize(float width, float height) {
        if (width == this.ownWidth && height == this.ownHeight)
            return;
        this.ownWidth = width;
        this.ownHeight = height;
        markDirty();
    }

    protected void setContent(Content content) {
        if (this.content != null)
            throw new IllegalStateException("Content has already been set");
        this.content = content;
        canvas.setOnInitCallback(() -> content.init(canvas));
        canvas.setOnResizeCallback(() -> content.resize(canvas));
        canvas.setOnUpdateCallback(() -> content.update(canvas));
        canvas.setOnReleaseCallback(content::release);
    }

    protected void releaseNative() {
        canvas.close();
    }

    @Override
    protected void drawContent(Context ctx) {
        if (content == null)
            return;

        Vector2f screenSize = ctx.pose().transformDirection(contentBox.width, contentBox.height, new Vector2f());
        int width = GMath.ceil(screenSize.x);
        int height = GMath.ceil(screenSize.y);
        canvas.scheduleResize(width, height);
        if (content.shouldUpdate())
            canvas.scheduleUpdate();

        Sprite sprite = canvas.getGlSprite();
        if (sprite != null)
            ctx.drawTexture(sprite, contentBox, 0xffffffff);
    }

    public interface Content {
        /**
         * Called when the canvas is initialized.
         * This method is called on the Thorvg thread, so it should not perform any OpenGL operations.
         */
        void init(AsyncCanvas canvas);

        /**
         * Called when the canvas is resized.
         * This method is called on the Thorvg thread, so it should not perform any OpenGL operations.
         */
        void resize(AsyncCanvas canvas);

        /**
         * Called when the canvas is updated.
         * This method is called on the Thorvg thread, so it should not perform any OpenGL operations.
         *
         * @return true if the content needs to be redrawn, false otherwise.
         */
        boolean update(AsyncCanvas canvas);

        /**
         * Called before drawing the content. If this method returns true, onCanvasUpdateAsync will be called to update the content.
         */
        boolean shouldUpdate();

        /**
         * Called when the canvas is released.
         * This method is called on the Thorvg thread, so it should not perform any OpenGL operations.
         */
        void release();
    }
}
