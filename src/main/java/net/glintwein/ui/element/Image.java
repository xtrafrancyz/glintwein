package net.glintwein.ui.element;

import net.glintwein.ui.data.Size;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.texture.Texture;

public class Image extends LeafElement {
    private Texture texture;

    public Image(Texture texture) {
        setMeasureFunction((width, widthMode, height, heightMode) -> {
            float aspectRatio = (float) this.texture.getWidth() / this.texture.getHeight();
            if (widthMode == SizeMode.EXACTLY) {
                float measuredHeight = width / aspectRatio;
                return new Size(width, measuredHeight);
            } else if (heightMode == SizeMode.EXACTLY) {
                float measuredWidth = height * aspectRatio;
                return new Size(measuredWidth, height);
            } else if (widthMode == SizeMode.AT_MOST && heightMode == SizeMode.AT_MOST) {
                float maxWidth = width;
                float maxHeight = height;
                float widthBasedHeight = maxWidth / aspectRatio;
                if (widthBasedHeight <= maxHeight) {
                    return new Size(maxWidth, widthBasedHeight);
                } else {
                    float heightBasedWidth = maxHeight * aspectRatio;
                    return new Size(heightBasedWidth, maxHeight);
                }
            } else if (widthMode == SizeMode.AT_MOST) {
                float measuredHeight = Math.min(width, this.texture.getWidth()) / aspectRatio;
                return new Size(width, measuredHeight);
            } else if (heightMode == SizeMode.AT_MOST) {
                float measuredWidth = Math.min(height, this.texture.getHeight()) * aspectRatio;
                return new Size(measuredWidth, height);
            }
            return new Size(this.texture.getWidth(), this.texture.getHeight());
        });

        setTexture(texture);
    }

    public void setTexture(Texture texture) {
        if (texture == this.texture)
            return;
        this.texture = texture;
        setAspectRatio(texture.getWidth(), texture.getHeight());
        markDirty();
    }

    @Override
    public void draw(Context ctx) {
        super.draw(ctx);
        ctx.drawTexture(texture.getSprite(), contentBox, borderRadius, 0xFFFFFFFF);
    }
}
