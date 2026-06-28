package net.glintwein.ui;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.RootElement;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.util.GMath;
import net.glintwein.util.KVStore;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Window {
    private static final float RESIZE_HANDLE_SIZE = 12f;

    private final String id;
    public final RootElement root;
    public WindowManager windowManager;

    protected boolean dragged;
    private float dragStartX;
    private float dragStartY;
    private float dragCurrentPosX;
    private float dragCurrentPosY;
    private boolean moved;

    private boolean resizeable = false;
    private boolean resizing;
    private float resizeStartMouseX;
    private float resizeStartMouseY;
    private float resizeStartScale;
    private float resizeStartContentWidth;
    private float resizeStartContentHeight;

    private float scaleMin = 0.2f;
    private float scaleMax = 4f;

    private Anchor anchor = Anchor.TOP_LEFT;
    private float posXPct = 0f;
    private float posYPct = 0f;
    private float scale = 1f;

    private static final float SNAP_THRESHOLD = 12f;
    private Float activeGuideX = null;
    private Float activeGuideY = null;

    public Window(String id) {
        this.id = id;
        root = new RootElement();
        root.setPositionType(PositionType.ABSOLUTE);
        loadPosition();
    }

    public void tick(float mouseX, float mouseY, boolean canHover) {
        updateMouse(mouseX, mouseY, canHover);
        root.tick();
        root.calculateLayout(-1, -1);
    }

    public void draw(Context ctx) {
        float screenW = GlobalUIState.getScaledWidth();
        float screenH = GlobalUIState.getScaledHeight();

        if (dragged && moved) {
            drawGuides(ctx, screenW, screenH);
        }

        ctx.pose().pushMatrix();
        ctx.pose().translate(getScreenXY());
        if (scale != 1f)
            ctx.pose().scale(scale);
        root.draw(ctx);
        if (resizeable && (resizing || root.isHoveredSelf()))
            drawResizeHandle(ctx, RESIZE_HANDLE_SIZE, 0xCCFFFFFF);
        ctx.pose().popMatrix();
    }

    protected void drawResizeHandle(Context ctx, float handleSize, int color) {
        float scaledHandleSize = Math.min(handleSize / scale, Math.min(root.getComputedWidth(), root.getComputedHeight()));
        float contentW = root.getComputedWidth();
        float contentH = root.getComputedHeight();

        float lineWidth = 1f / scale;
        float inset = 2f / scale;
        float spacing = 3f / scale;

        float right = contentW - inset;
        float bottom = contentH - inset;
        float start = Math.max(0f, Math.min(scaledHandleSize - inset, scaledHandleSize * 0.75f));

        ctx.drawLine(right - start, bottom, right, bottom - start, lineWidth, BorderRadius.ZERO, color);
        ctx.drawLine(right - start + spacing, bottom, right, bottom - start + spacing, lineWidth, BorderRadius.ZERO, color);
    }

    private void drawGuides(Context ctx, float screenW, float screenH) {
        int staticColor = 0x33FFFFFF; // Полупрозрачный для фона
        int activeColor = 0xFFFFFFFF; // Яркий для активной привязки

        // Всегда рендерим статичные направляющие: 5%, 50%, 95%
        float[] staticX = {screenW * 0.05f, screenW * 0.5f, screenW * 0.95f};
        float[] staticY = {screenH * 0.05f, screenH * 0.5f, screenH * 0.95f};

        float pixSize = GlobalUIState.getPixelSize();

        for (float x : staticX) ctx.drawRect(x, 0, pixSize, screenH, staticColor);
        for (float y : staticY) ctx.drawRect(0, y, screenW, pixSize, staticColor);

        // Поверх рисуем ярким те, к которым "прилипли" сейчас
        if (activeGuideX != null) ctx.drawRect(activeGuideX, 0, pixSize, screenH, activeColor);
        if (activeGuideY != null) ctx.drawRect(0, activeGuideY, screenW, pixSize, activeColor);
    }

    private void updateMouse(float mouseX, float mouseY, boolean canHover) {
        if (resizing) {
            updateResize(mouseX, mouseY);
        } else if (dragged) {
            moved = moved || dragStartX != mouseX || dragStartY != mouseY;

            float deltaX = mouseX - dragStartX;
            float deltaY = mouseY - dragStartY;
            dragStartX = mouseX;
            dragStartY = mouseY;

            dragCurrentPosX += deltaX;
            dragCurrentPosY += deltaY;

            applySnapping(dragCurrentPosX, dragCurrentPosY);
        } else {
            activeGuideX = null;
            activeGuideY = null;
        }

        Vector2f xy = getScreenXY();
        root.updateMouse((mouseX - xy.x) / scale, (mouseY - xy.y) / scale, canHover);
    }

    private void updateResize(float mouseX, float mouseY) {
        float dx = mouseX - resizeStartMouseX;
        float dy = mouseY - resizeStartMouseY;

        float denom = resizeStartContentWidth * resizeStartContentWidth + resizeStartContentHeight * resizeStartContentHeight;
        if (denom <= 0f)
            return;

        float newScale = resizeStartScale + ((resizeStartContentWidth * dx) + (resizeStartContentHeight * dy)) / denom;
        setScale(newScale);
    }

    private void applySnapping(float rawX, float rawY) {
        float screenW = GlobalUIState.getScaledWidth();
        float screenH = GlobalUIState.getScaledHeight();
        float w = getComputedWidth();
        float h = getComputedHeight();

        List<Float> guidesX = new ArrayList<>();
        List<Float> guidesY = new ArrayList<>();

        // 1. Сетка экрана (5%, 50%, 95%)
        guidesX.add(screenW * 0.05f);
        guidesX.add(screenW * 0.5f);
        guidesX.add(screenW * 0.95f);

        guidesY.add(screenH * 0.05f);
        guidesY.add(screenH * 0.5f);
        guidesY.add(screenH * 0.95f);

        // =========================================================
        // 2. ДИНАМИЧЕСКИЕ НАПРАВЛЯЮЩИЕ ОТ ДРУГИХ ОКОН
        // =========================================================

        List<Window> otherWindows = windowManager == null ? Collections.emptyList() : windowManager.windows;
        for (Window other : otherWindows) {
            if (other == this)
                continue; // Себя не учитываем

            Vector2f otherXY = other.getScreenXY();
            float otherW = other.getComputedWidth();
            float otherH = other.getComputedHeight();

            // Добавляем края и центры других окон как магниты
            guidesX.add(otherXY.x);
            guidesX.add(otherXY.x + otherW / 2f);
            guidesX.add(otherXY.x + otherW);

            guidesY.add(otherXY.y);
            guidesY.add(otherXY.y + otherH / 2f);
            guidesY.add(otherXY.y + otherH);
        }

        // Точки нашего окна (лево, центр, право / верх, центр, низ)
        float[] windowPointsX = {rawX, rawX + w / 2f, rawX + w};
        float[] windowPointsY = {rawY, rawY + h / 2f, rawY + h};

        float bestX = rawX;
        float bestY = rawY;

        float minDiffX = SNAP_THRESHOLD;
        float minDiffY = SNAP_THRESHOLD;

        activeGuideX = null;
        activeGuideY = null;

        int xIndex = -1;
        int yIndex = -1;

        for (float guideX : guidesX) {
            for (int i = 0; i < windowPointsX.length; i++) {
                float diff = Math.abs(guideX - windowPointsX[i]);
                if (diff < minDiffX) {
                    minDiffX = diff;
                    activeGuideX = guideX;
                    xIndex = i;
                    if (i == 0) bestX = guideX;
                    else if (i == 1) bestX = guideX - w / 2f;
                    else if (i == 2) bestX = guideX - w;
                }
            }
        }

        for (float guideY : guidesY) {
            for (int i = 0; i < windowPointsY.length; i++) {
                float diff = Math.abs(guideY - windowPointsY[i]);
                if (diff < minDiffY) {
                    minDiffY = diff;
                    activeGuideY = guideY;
                    yIndex = i;
                    if (i == 0) bestY = guideY;
                    else if (i == 1) bestY = guideY - h / 2f;
                    else if (i == 2) bestY = guideY - h;
                }
            }
        }

        if (xIndex == -1) {
            float bestDiffX = Math.abs(rawX - 0);
            xIndex = 0;
            float abs = Math.abs(rawX + w / 2f - screenW / 2f);
            if (abs < bestDiffX) {
                bestDiffX = abs;
                xIndex = 1;
            }
            abs = Math.abs(rawX + w - screenW);
            if (abs < bestDiffX) {
                xIndex = 2;
            }
        }
        if (yIndex == -1) {
            float bestDiffY = Math.abs(rawY - 0);
            yIndex = 0;
            float abs = Math.abs(rawY + h / 2f - screenH / 2f);
            if (abs < bestDiffY) {
                bestDiffY = abs;
                yIndex = 1;
            }
            abs = Math.abs(rawY + h - screenH);
            if (abs < bestDiffY) {
                yIndex = 2;
            }
        }

        anchor = Anchor.values()[yIndex * 3 + xIndex];

        switch (anchor) {
            case TOP_LEFT:
                break;
            case TOP:
                bestX += w / 2f;
                break;
            case TOP_RIGHT:
                bestX += w;
                break;
            case LEFT:
                bestY += h / 2f;
                break;
            case CENTER:
                bestX += w / 2f;
                bestY += h / 2f;
                break;
            case RIGHT:
                bestX += w;
                bestY += h / 2f;
                break;
            case BOTTOM_LEFT:
                bestY += h;
                break;
            case BOTTOM:
                bestX += w / 2f;
                bestY += h;
                break;
            case BOTTOM_RIGHT:
                bestX += w;
                bestY += h;
                break;
        }

        // Переводим пиксельное смещение в проценты от ширины/высоты экрана
        posXPct = (screenW > 0) ? (bestX / screenW) * 100f : 0f;
        posYPct = (screenH > 0) ? (bestY / screenH) * 100f : 0f;
    }

    public void invalidateLayout() {
        root.invalidateLayout();
    }

    public boolean onMousePress(float mouseX, float mouseY, int button) {
        Vector2f xy = getScreenXY();
        float localX = (mouseX - xy.x) / scale;
        float localY = (mouseY - xy.y) / scale;

        if (button == 0 && resizeable && isOnResizeHandle(localX, localY)) {
            startResizing(mouseX, mouseY);
            return true;
        }

        boolean handled = root.handleMousePress(localX, localY, button);

        if (!handled && root.isHovered()) {
            dragged = true;
            resizing = false;
            moved = false;
            dragStartX = mouseX;
            dragStartY = mouseY;
            dragCurrentPosX = xy.x;
            dragCurrentPosY = xy.y;
            handled = true;
        }
        return handled;
    }

    public boolean onMouseRelease(float mouseX, float mouseY, int button) {
        if (dragged) savePosition();
        dragged = false;
        resizing = false;
        activeGuideX = null;
        activeGuideY = null;
        Vector2f xy = getScreenXY();
        return root.handleMouseRelease((mouseX - xy.x) / scale, (mouseY - xy.y) / scale, button);
    }

    public boolean onMouseScroll(float mouseX, float mouseY, float horizontal, float vertical) {
        Vector2f xy = getScreenXY();
        return root.handleMouseScroll((mouseX - xy.x) / scale, (mouseY - xy.y) / scale, horizontal, vertical);
    }

    public Vector2f getScreenXY() {
        float x = (posXPct / 100f) * GlobalUIState.getScaledWidth();
        float y = (posYPct / 100f) * GlobalUIState.getScaledHeight();
        float width = getComputedWidth();
        float height = getComputedHeight();
        switch (anchor) {
            case TOP_LEFT:
                break;
            case TOP_RIGHT:
                x -= width;
                break;
            case BOTTOM_LEFT:
                y -= height;
                break;
            case BOTTOM_RIGHT:
                x -= width;
                y -= height;
                break;
            case CENTER:
                x -= width / 2f;
                y -= height / 2f;
                break;
            case TOP:
                x -= width / 2f;
                break;
            case BOTTOM:
                x -= width / 2f;
                y -= height;
                break;
            case LEFT:
                y -= height / 2f;
                break;
            case RIGHT:
                x -= width;
                y -= height / 2f;
                break;
        }

        return new Vector2f(GlobalUIState.snapToPixel(x), GlobalUIState.snapToPixel(y));
    }

    public void setResizeable(boolean resizeable) {
        if (this.resizeable == resizeable)
            return;
        this.resizeable = resizeable;
        this.resizing = false;
    }

    public boolean isResizeable() {
        return resizeable;
    }

    public void setScaleLimit(float minScale, float maxScale) {
        if (minScale > maxScale)
            throw new IllegalArgumentException("minScale cannot be greater than maxScale");
        if (minScale <= 0f || maxScale <= 0f)
            throw new IllegalArgumentException("Scale limits must be positive");
        this.scaleMin = minScale;
        this.scaleMax = maxScale;
    }

    public void setScale(float newScale) {
        float clampedScale = GMath.clamp(newScale, scaleMin, scaleMax);
        if (scale == clampedScale)
            return;
        Vector2f topLeft = getScreenXY();
        this.scale = clampedScale;
        applyTopLeft(topLeft.x, topLeft.y);
        savePosition();
    }

    private void applyTopLeft(float topLeftX, float topLeftY) {
        float screenW = GlobalUIState.getScaledWidth();
        float screenH = GlobalUIState.getScaledHeight();
        float width = getComputedWidth();
        float height = getComputedHeight();

        float anchorX = topLeftX;
        float anchorY = topLeftY;

        switch (anchor) {
            case TOP_LEFT:
                break;
            case TOP:
                anchorX += width / 2f;
                break;
            case TOP_RIGHT:
                anchorX += width;
                break;
            case LEFT:
                anchorY += height / 2f;
                break;
            case CENTER:
                anchorX += width / 2f;
                anchorY += height / 2f;
                break;
            case RIGHT:
                anchorX += width;
                anchorY += height / 2f;
                break;
            case BOTTOM_LEFT:
                anchorY += height;
                break;
            case BOTTOM:
                anchorX += width / 2f;
                anchorY += height;
                break;
            case BOTTOM_RIGHT:
                anchorX += width;
                anchorY += height;
                break;
        }

        posXPct = (screenW > 0) ? (anchorX / screenW) * 100f : 0f;
        posYPct = (screenH > 0) ? (anchorY / screenH) * 100f : 0f;
    }

    private boolean isOnResizeHandle(float localX, float localY) {
        float handleSize = Math.min(RESIZE_HANDLE_SIZE / scale, Math.min(root.getComputedWidth(), root.getComputedHeight()));
        float width = root.getComputedWidth();
        float height = root.getComputedHeight();
        return localX >= width - handleSize && localY >= height - handleSize;
    }

    private void startResizing(float mouseX, float mouseY) {
        resizing = true;
        dragged = false;
        moved = false;
        activeGuideX = null;
        activeGuideY = null;
        resizeStartMouseX = mouseX;
        resizeStartMouseY = mouseY;
        resizeStartScale = scale;
        resizeStartContentWidth = root.getComputedWidth();
        resizeStartContentHeight = root.getComputedHeight();
    }

    public float getScale() {
        return scale;
    }

    public float getComputedWidth() {
        return root.getComputedWidth() * scale;
    }

    public float getComputedHeight() {
        return root.getComputedHeight() * scale;
    }

    private String savePrefix() {
        return "window_" + id + "_";
    }

    private void savePosition() {
        String prefix = savePrefix();
        KVStore.put(prefix + "anchor", anchor.name());
        KVStore.put(prefix + "pos_x_pct", posXPct);
        KVStore.put(prefix + "pos_y_pct", posYPct);
        KVStore.put(prefix + "scale", scale);
    }

    private void loadPosition() {
        String prefix = savePrefix();
        anchor = Anchor.fromString(KVStore.getString(prefix + "anchor", ""));
        posXPct = KVStore.getFloat(prefix + "pos_x_pct", 0f);
        posYPct = KVStore.getFloat(prefix + "pos_y_pct", 0f);
        scale = KVStore.getFloat(prefix + "scale", 1f);
    }

    private enum Anchor {
        TOP_LEFT, TOP, TOP_RIGHT, LEFT, CENTER, RIGHT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;

        static Anchor fromString(String name) {
            try {
                return Anchor.valueOf(name);
            } catch (IllegalArgumentException e) {
                return TOP_LEFT;
            }
        }
    }
}
