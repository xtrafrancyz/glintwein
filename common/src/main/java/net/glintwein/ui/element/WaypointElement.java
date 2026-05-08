package net.glintwein.ui.element;

import net.glintwein.Glintwein;
import net.glintwein.platform.Platform;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.Box;
import net.glintwein.ui.data.Display;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.util.Easing;
import net.glintwein.ui.util.GMath;
import net.glintwein.ui.util.LayoutBoxLerp;
import org.joml.Vector3f;

public class WaypointElement extends Element {
    private Vector3f targetPos = new Vector3f();

    private float lastTopPos = Float.NaN;
    private float lastLeftPos = Float.NaN;
    private float lastSizeSum = Float.NaN;

    public WaypointElement() {
        setPositionType(PositionType.ABSOLUTE);
    }

    public void setTargetPos(Vector3f targetPos) {
        this.targetPos = targetPos;
    }

    public Vector3f getTargetPos() {
        return targetPos;
    }

    @Override
    public void enableLayoutLerp(float durationMs, Easing easing, boolean lerpPosition, boolean lerpSize) {
        layoutLerp = new LayoutLerp(durationMs, easing);
    }

    @Override
    public void tick() {
        super.tick();
        Vector3f target = getTargetPos();
        if (target == null)
            return;
        calculateScreenPosition(target);
    }

    public boolean isNearScreenCenter(float distance) {
        if (getDisplayType() == Display.NONE)
            return false;
        float centerX = GlobalUIState.getScaledWidth() / 2;
        float centerY = GlobalUIState.getScaledHeight() / 2;
        float distanceSquared = GMath.square(lastLeftPos - centerX) + GMath.square(lastTopPos - centerY);
        return distanceSquared <= distance * distance;
    }

    private void calculateScreenPosition(Vector3f target) {
        target = target.sub(Platform.render().getCameraPos(), new Vector3f())
            .mulPosition(Platform.render().getWorldViewMatrix())
            .mulPosition(Platform.render().getWorldProjMatrix());

        // VIBE CODING STARTS HERE

        float screenX = 0;
        float screenY = 0;

        // --- Define padding and boundaries for screen edges ---
        final float HALF_MARKER_W = getComputedWidth() / 2;
        final float HALF_MARKER_H = getComputedHeight() / 2;
        final float SCREEN_W = GlobalUIState.getScaledWidth();
        final float SCREEN_H = GlobalUIState.getScaledHeight();

        final float MIN_X = HALF_MARKER_W;
        final float MAX_X = SCREEN_W - HALF_MARKER_W;
        final float MIN_Y = HALF_MARKER_H;
        final float MAX_Y = SCREEN_H - HALF_MARKER_H;

        // 1. Check if the target is behind the camera (z < 0) or too close to the camera (z is zero/near-zero)
        if (target.z < 0.001f) {
            setDisplay(Display.NONE);
            return;
        }

        // Convert to Normalized Device Coordinates (NDC)
        float ndcX = target.x / target.z;
        float ndcY = target.y / target.z;

        // Convert NDC to screen coordinates (origin at top-left)
        screenX = (ndcX + 1) * 0.5f * SCREEN_W;
        screenY = (1 - (ndcY + 1) * 0.5f) * SCREEN_H;

        // Check if the projected point is outside the screen frustum (NDC check).
        if (ndcX < -1.0f || ndcX > 1.0f || ndcY < -1.0f || ndcY > 1.0f) {
            setDisplay(Display.NONE);
            return;
        }

        // Target is fully on-screen. We use the projected position directly.
        screenX = GMath.clamp(screenX, MIN_X, MAX_X);
        screenY = GMath.clamp(screenY, MIN_Y, MAX_Y);

        float sizeSum = HALF_MARKER_W * 71 + HALF_MARKER_H;
        if (sizeSum != lastSizeSum) {
            lastSizeSum = sizeSum;
            lastTopPos = Float.NaN;
            lastLeftPos = Float.NaN;
        }
        if (lastTopPos != screenY) {
            lastTopPos = screenY;
            setPosition(Edge.TOP, screenY - HALF_MARKER_H);
        }
        if (lastLeftPos != screenX) {
            lastLeftPos = screenX;
            setPosition(Edge.LEFT, screenX - HALF_MARKER_W);
        }

        setDisplay(Display.FLEX);
    }

    @Override
    public void draw(Context ctx) {
        ctx.pushDrawPriority(-100);
        super.draw(ctx);
        ctx.popDrawPriority(-100);
    }

    private static class LayoutLerp extends LayoutBoxLerp {
        public LayoutLerp(float durationMs, Easing easing) {
            super(durationMs, easing, false, true);
        }

        @Override
        public void startAnimation(Box border, Box padding, Box content) {
            border0.setXY(border);
            padding0.setXY(padding);
            content0.setXY(content);
            border1.setXY(border);
            padding1.setXY(padding);
            content1.setXY(content);

            float epsilon = GlobalUIState.getPixelSize() * 1.5f;

            if (!(Math.abs(border1.height - border.height) < epsilon &&
                Math.abs(border1.width - border.width) < epsilon &&
                Math.abs(padding1.height - padding.height) < epsilon &&
                Math.abs(padding1.width - padding.width) < epsilon &&
                Math.abs(content1.height - content.height) < epsilon &&
                Math.abs(content1.width - content.width) < epsilon)) {
                // start animation
                apply(border0, padding0, content0);
                this.endTime = Glintwein.time + (long) durationMs;
            } else {
                // Sizes are close enough, skip animation but update values to target immediately
            }

            content1.setSize(content);
            padding1.setSize(padding);
            border1.setSize(border);

            apply(border, padding, content);
        }
    }
}
