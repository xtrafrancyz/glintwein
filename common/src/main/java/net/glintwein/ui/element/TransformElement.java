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

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    protected void handleMouseMoved(float mouseX, float mouseY, boolean canHover) {
        super.handleMouseMoved(scaleMouseX(mouseX), scaleMouseY(mouseY), canHover);
    }

    @Override
    protected boolean handleMousePress(float mouseX, float mouseY, int button) {
        return super.handleMousePress(scaleMouseX(mouseX), scaleMouseY(mouseY), button);
    }

    @Override
    protected boolean handleMouseRelease(float mouseX, float mouseY, int button, boolean blocked) {
        return super.handleMouseRelease(scaleMouseX(mouseX), scaleMouseY(mouseY), button, blocked);
    }

    @Override
    protected boolean handleMouseScroll(float mouseX, float mouseY, float horizontal, float vertical) {
        return super.handleMouseScroll(scaleMouseX(mouseX), scaleMouseY(mouseY), horizontal, vertical);
    }

    private float scaleMouseX(float mouseX) {
        return (mouseX - borderBox.x - borderBox.width / 2) / scaleX + borderBox.x + borderBox.width / 2;
    }

    private float scaleMouseY(float mouseY) {
        return (mouseY - borderBox.y - borderBox.height / 2) / scaleY + borderBox.y + borderBox.height / 2;
    }

    @Override
    public void draw(Context ctx) {
        ctx.pose().pushMatrix();
        if (scaleX != 1 || scaleY != 1) {
            ctx.pose().translate(borderBox.x + borderBox.width / 2, borderBox.y + borderBox.height / 2);
            ctx.pose().scale(scaleX, scaleY);
            ctx.pose().translate(-(borderBox.x + borderBox.width / 2), -(borderBox.y + borderBox.height / 2));
        }
        if (rotation != 0)
            ctx.pose().rotateAbout(rotation * GMath.DEG_TO_RAD, borderBox.x + borderBox.width / 2, borderBox.y + borderBox.height / 2);
        super.draw(ctx);
        ctx.pose().popMatrix();
    }
}
