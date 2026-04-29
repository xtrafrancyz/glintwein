package net.glintwein.ui.util;

import net.glintwein.Glintwein;
import net.glintwein.ui.data.Box;

public class LayoutBoxLerp {
    public final Box border0 = new Box();
    public final Box border1 = new Box();
    public final Box padding0 = new Box();
    public final Box padding1 = new Box();
    public final Box content0 = new Box();
    public final Box content1 = new Box();

    public final float durationMs;
    public final Easing easing;
    private final boolean lerpPosition;
    private final boolean lerpSize;

    protected boolean first = true;
    protected long endTime;

    public LayoutBoxLerp(float durationMs, Easing easing, boolean lerpPosition, boolean lerpSize) {
        this.durationMs = durationMs;
        this.easing = easing;
        this.lerpPosition = lerpPosition;
        this.lerpSize = lerpSize;
    }

    public void animate(Box border, Box padding, Box content) {
        if (first) {
            first = false;
            border0.set(border);
            padding0.set(padding);
            content0.set(content);
            border1.set(border);
            padding1.set(padding);
            content1.set(content);
            return;
        }

        startAnimation(border, padding, content);
    }

    protected void startAnimation(Box border, Box padding, Box content) {
        // Save current values
        apply(border0, padding0, content0);

        // Set animation end time
        this.endTime = Glintwein.time + (long) durationMs;

        // Set target values
        content1.set(content);
        padding1.set(padding);
        border1.set(border);

        border.set(border0);
        padding.set(padding0);
        content.set(content0);
    }

    public void apply(Box border, Box padding, Box content) {
        if (endTime == 0)
            return;

        float t = 1.0f - (endTime - Glintwein.time) / durationMs;
        if (t >= 1.0f) {
            border0.set(border1);
            padding0.set(padding1);
            content0.set(content1);
            border.set(border1);
            padding.set(padding1);
            content.set(content1);
            endTime = 0;
        } else {
            t = easing.ease(t);
            if (lerpPosition) {
                border.x = GMath.lerp(t, border0.x, border1.x);
                border.y = GMath.lerp(t, border0.y, border1.y);
                padding.x = GMath.lerp(t, padding0.x, padding1.x);
                padding.y = GMath.lerp(t, padding0.y, padding1.y);
                content.x = GMath.lerp(t, content0.x, content1.x);
                content.y = GMath.lerp(t, content0.y, content1.y);
            } else {
                border.setXY(border1);
                padding.setXY(padding1);
                content.setXY(content1);
            }
            if (lerpSize) {
                border.width = GMath.lerp(t, border0.width, border1.width);
                border.height = GMath.lerp(t, border0.height, border1.height);
                padding.width = GMath.lerp(t, padding0.width, padding1.width);
                padding.height = GMath.lerp(t, padding0.height, padding1.height);
                content.width = GMath.lerp(t, content0.width, content1.width);
                content.height = GMath.lerp(t, content0.height, content1.height);
            } else {
                border.setSize(border1);
                padding.setSize(padding1);
                content.setSize(content1);
            }
        }
    }
}
