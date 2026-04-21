package net.glintwein.ui.element;

import net.glintwein.platform.Platform;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.Display;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.util.GMath;
import org.joml.Vector3f;

public class WaypointElement extends Element {
    private Vector3f targetPos = new Vector3f();

    private float lastTopPos = Float.NaN;
    private float lastLeftPos = Float.NaN;

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
        target = target.sub(Platform.get().getRender().getCameraPos(), new Vector3f())
            .mulPosition(Platform.get().getRender().getWorldViewMatrix())
            .mulPosition(Platform.get().getRender().getWorldProjMatrix());

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
        screenX = Math.max(MIN_X, Math.min(MAX_X, screenX));
        screenY = Math.max(MIN_Y, Math.min(MAX_Y, screenY));

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
}
