package net.glintwein.ui;

import net.glintwein.GlintweinFabricMod;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.RootElement;
import net.glintwein.ui.render.command.Context;
import net.glintwein.util.KVStore;
import org.joml.Vector2f;

public class Window {
    public final WindowManager manager;
    private final String id;
    public final RootElement root;

    private boolean dragged;
    private float dragStartX;
    private float dragStartY;
    private float dragCurrentPosX;
    private float dragCurrentPosY;
    private Anchor anchor = Anchor.TOP_LEFT;
    private float posX = 0f;
    private float posY = 0f;

    public Window(WindowManager manager, String id) {
        this.manager = manager;
        this.id = id;
        root = new RootElement();
        root.setPositionType(PositionType.ABSOLUTE);
        loadPosition();
    }

    public void tick(float mouseX, float mouseY) {
        updateMouse(mouseX, mouseY);
        root.tick();
        root.calculateLayout(-1, -1);
    }

    public void draw(Context ctx) {
        ctx.pose().pushMatrix();

        // When dragging, highlight the current anchor reference point on the screen
        if (dragged) {
            float screenWidth = manager.getScreenWidth();
            float screenHeight = manager.getScreenHeight();

            float ax = 0f;
            float ay = 0f;
            switch (anchor) {
                case TOP_LEFT:
                    ax = 0f;
                    ay = 0f;
                    break;
                case TOP_RIGHT:
                    ax = screenWidth;
                    ay = 0f;
                    break;
                case BOTTOM_LEFT:
                    ax = 0f;
                    ay = screenHeight;
                    break;
                case BOTTOM_RIGHT:
                    ax = screenWidth;
                    ay = screenHeight;
                    break;
                case CENTER:
                    ax = screenWidth / 2f;
                    ay = screenHeight / 2f;
                    break;
                case TOP:
                    ax = screenWidth / 2f;
                    ay = 0f;
                    break;
                case BOTTOM:
                    ax = screenWidth / 2f;
                    ay = screenHeight;
                    break;
                case LEFT:
                    ax = 0f;
                    ay = screenHeight / 2f;
                    break;
                case RIGHT:
                    ax = screenWidth;
                    ay = screenHeight / 2f;
                    break;
            }

            // Draw a small semi-transparent rectangle to indicate the anchor point
            float size = 12f;
            int color = 0xAAFFFF00; // ARGB: semi-transparent yellow
            ctx.drawRect(ax - size / 2f, ay - size / 2f, size, size, color);
        }

        ctx.pose().translate(getScreenXY());

        root.draw(ctx);

        ctx.pose().popMatrix();
    }

    private void updateMouse(float mouseX, float mouseY) {
        if (dragged) {
            float deltaX = mouseX - dragStartX;
            float deltaY = mouseY - dragStartY;
            dragStartX = mouseX;
            dragStartY = mouseY;

            dragCurrentPosX += deltaX;
            dragCurrentPosY += deltaY;
            setWindowPosition(dragCurrentPosX, dragCurrentPosY);

        }

        Vector2f xy = getScreenXY();
        root.updateMouse(mouseX - xy.x, mouseY - xy.y);
    }

    private void setWindowPosition(float x, float y) {
        // Choose the anchor whose screen reference point is closest to the supplied
        // top-left screen coordinates (x,y), and compute posX/posY so that
        // getScreenXY() will return the same top-left coordinates for that anchor.

        float screenWidth = manager.getScreenWidth();
        float screenHeight = manager.getScreenHeight();
        float width = root.getComputedWidth();
        float height = root.getComputedHeight();

        if (Float.isNaN(width)) width = 0f;
        if (Float.isNaN(height)) height = 0f;

        float bestDist = Float.POSITIVE_INFINITY;
        Anchor bestAnchor = Anchor.TOP_LEFT;
        float bestPosX = x;
        float bestPosY = y;

        for (Anchor a : Anchor.values()) {
            float candidatePosX = 0f;
            float candidatePosY = 0f;
            float ax = 0f;
            float ay = 0f;

            switch (a) {
                case TOP_LEFT:
                    ax = 0f;
                    ay = 0f;
                    candidatePosX = x;
                    candidatePosY = y;
                    break;
                case TOP_RIGHT:
                    ax = screenWidth;
                    ay = 0f;
                    candidatePosX = screenWidth - width - x;
                    candidatePosY = y;
                    break;
                case BOTTOM_LEFT:
                    ax = 0f;
                    ay = screenHeight;
                    candidatePosX = x;
                    candidatePosY = screenHeight - height - y;
                    break;
                case BOTTOM_RIGHT:
                    ax = screenWidth;
                    ay = screenHeight;
                    candidatePosX = screenWidth - width - x;
                    candidatePosY = screenHeight - height - y;
                    break;
                case CENTER:
                    ax = screenWidth / 2f;
                    ay = screenHeight / 2f;
                    candidatePosX = x - (screenWidth - width) / 2f;
                    candidatePosY = y - (screenHeight - height) / 2f;
                    break;
                case TOP:
                    ax = screenWidth / 2f;
                    ay = 0f;
                    candidatePosX = x - (screenWidth - width) / 2f;
                    candidatePosY = y;
                    break;
                case BOTTOM:
                    ax = screenWidth / 2f;
                    ay = screenHeight;
                    candidatePosX = x - (screenWidth - width) / 2f;
                    candidatePosY = screenHeight - height - y;
                    break;
                case LEFT:
                    ax = 0f;
                    ay = screenHeight / 2f;
                    candidatePosX = x;
                    candidatePosY = y - (screenHeight - height) / 2f;
                    break;
                case RIGHT:
                    ax = screenWidth;
                    ay = screenHeight / 2f;
                    candidatePosX = screenWidth - width - x;
                    candidatePosY = y - (screenHeight - height) / 2f;
                    break;
            }

            float dx = x - ax;
            float dy = y - ay;
            float dist2 = dx * dx + dy * dy;

            if (dist2 < bestDist) {
                bestDist = dist2;
                bestAnchor = a;
                bestPosX = candidatePosX;
                bestPosY = candidatePosY;
            }
        }

        anchor = bestAnchor;
        posX = bestPosX;
        posY = bestPosY;
    }

    public boolean onMousePress(float mouseX, float mouseY, int button) {
        Vector2f xy = getScreenXY();
        boolean handled = root.handleMousePress(mouseX - xy.x, mouseY - xy.y, button);

        if (!handled && root.isHovered()) {
            dragged = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            dragCurrentPosX = xy.x;
            dragCurrentPosY = xy.y;
            handled = true;
        }

        return handled;
    }

    public boolean onMouseRelease(float mouseX, float mouseY, int button) {
        if (dragged) {
            savePosition();
        }
        dragged = false;
        Vector2f xy = getScreenXY();
        return root.handleMouseRelease(mouseX - xy.x, mouseY - xy.y, button);
    }

    public boolean onMouseScroll(float mouseX, float mouseY, float amount, float vertical) {
        Vector2f xy = getScreenXY();
        return root.handleMouseScroll(mouseX - xy.x, mouseY - xy.y, amount, vertical);
    }

    private Vector2f getScreenXY() {
        float screenWidth = manager.getScreenWidth();
        float screenHeight = manager.getScreenHeight();
        float width = root.getComputedWidth();
        float height = root.getComputedHeight();

        float x = 0f;
        float y = 0f;

        switch (anchor) {
            case TOP_LEFT:
                x = posX;
                y = posY;
                break;
            case TOP_RIGHT:
                x = screenWidth - posX - width;
                y = posY;
                break;
            case BOTTOM_LEFT:
                x = posX;
                y = screenHeight - posY - height;
                break;
            case BOTTOM_RIGHT:
                x = screenWidth - posX - width;
                y = screenHeight - posY - height;
                break;
            case CENTER:
                x = (screenWidth - width) / 2 + posX;
                y = (screenHeight - height) / 2 + posY;
                break;
            case TOP:
                x = (screenWidth - width) / 2 + posX;
                y = posY;
                break;
            case BOTTOM:
                x = (screenWidth - width) / 2 + posX;
                y = screenHeight - posY - height;
                break;
            case LEFT:
                x = posX;
                y = (screenHeight - height) / 2 + posY;
                break;
            case RIGHT:
                x = screenWidth - posX - width;
                y = (screenHeight - height) / 2 + posY;
                break;
        }

        return new Vector2f(x, y);
    }

    private String savePrefix() {
        return "window_" + id + "_";
    }

    private void savePosition() {
        String prefix = savePrefix();
        KVStore.put(prefix + "anchor", anchor.name());
        KVStore.put(prefix + "pos_x", posX);
        KVStore.put(prefix + "pos_y", posY);
    }

    private void loadPosition() {
        String prefix = savePrefix();
        String anchorName = KVStore.getString(prefix + "anchor", "TOP_LEFT");
        try {
            anchor = Anchor.valueOf(anchorName);
        } catch (IllegalArgumentException e) {
            return;
        }
        posX = KVStore.getFloat(prefix + "pos_x", 0f);
        posY = KVStore.getFloat(prefix + "pos_y", 0f);
    }

    private enum Anchor {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER,
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }
}
