package net.glintwein.ui.element;

import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.util.GMath;

public class TransformElement extends Element {
    private float rotation = 0;
    private float scaleX = 1;
    private float scaleY = 1;

    public void setScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public void draw(Context ctx) {
        ctx.pose().pushMatrix();
        ctx.pose().scale(scaleX, scaleY);
        if (rotation != 0)
            ctx.pose().rotateAbout(rotation * GMath.DEG_TO_RAD, borderBox.x + borderBox.width / 2, borderBox.y + borderBox.height / 2);
        super.draw(ctx);
        ctx.pose().popMatrix();
    }
}
