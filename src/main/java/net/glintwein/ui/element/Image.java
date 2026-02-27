package net.glintwein.ui.element;

import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.texture.Texture;

public class Image extends LeafElement {
    private Texture texture;

    public Image(Texture texture) {
        setMeasureFunctionAspectRatio(() -> this.texture.getWidth(), () -> this.texture.getHeight());

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
    public void drawContent(Context ctx) {
        ctx.drawTexture(texture.getSprite(), contentBox, borderRadius, 0xFFFFFFFF);
    }
}
