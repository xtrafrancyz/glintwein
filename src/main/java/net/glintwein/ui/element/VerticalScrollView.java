package net.glintwein.ui.element;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Box;
import net.glintwein.ui.data.Overflow;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.util.Animated;
import net.glintwein.ui.util.Easing;
import net.glintwein.ui.util.GMath;
import org.lwjgl.util.yoga.Yoga;

import java.util.List;

public class VerticalScrollView extends Element {
    private final ScrollableContent content;

    private boolean hasOverflow;
    private float scrollOffsetY;

    private int barColor = 0x00000000;
    private int barThumbColor;
    private int barThumbHoverColor;
    private int barThumbActiveColor;
    private float barPadding;

    private final Box barThumbBox = new Box();
    private float barThumbOffsetY = 0;
    private final Animated.Float barThumbOffsetYAnim;
    private boolean thumbHovered = false;
    private boolean thumbDragged = false;
    private float dragStartY;
    private final Animated.Color barThumbColorAnim;
    private int barThumbColorComputed = barThumbColor;
    private float contentHeight = 0;
    private float visibleHeight = 0;
    private float trackHeight = 0;

    public VerticalScrollView() {
        setOverflow(Overflow.SCROLL);
        this.content = new ScrollableContent();
        content.setWidthPercent(100);
        super.addChild(content);

        trackAnimation(barThumbColorAnim = new Animated.Color(c -> barThumbColorComputed = c, barThumbColor));
        trackAnimation(barThumbOffsetYAnim = new Animated.Float(this::updateScrollOffsetFromAnim, -1));

        setBarWidth(6);
        setBarPadding(2);
        setBarThumbColorsLight();
    }

    public void setBarWidth(float width) {
        barThumbBox.width = width;
    }

    public void setBarBackgroundColor(int color) {
        this.barColor = color;
    }

    public void setBarThumbColor(int regular, int hover, int active) {
        this.barThumbColor = regular;
        this.barThumbHoverColor = hover;
        this.barThumbActiveColor = active;
    }

    public void setBarThumbColorsLight() {
        setBarThumbColor(0x66A0A0A0, 0x99787878, 0xCC606060);
    }

    public void setBarThumbColorsDark() {
        setBarThumbColor(0x66454545, 0x99606060, 0xCC808080);
    }

    public void setBarPadding(float padding) {
        this.barPadding = padding;
    }

    public boolean isThumbHovered() {
        return thumbHovered;
    }

    public boolean isThumbDragged() {
        return thumbDragged;
    }

    @Override
    public void addChild(Element child) {
        content.addChild(child);
    }

    @Override
    public void addChild(Element child, int index) {
        content.addChild(child, index);
    }

    @Override
    public List<Element> getChildren() {
        return content.getChildren();
    }

    @Override
    public void removeChild(Element child) {
        content.removeChild(child);
    }

    @Override
    public void clearChildren() {
        content.clearChildren();
    }

    @Override
    protected void propagateLayoutRead() {
        super.propagateLayoutRead();

        this.contentHeight = content.borderBox.height;
        this.visibleHeight = contentBox.height;
        this.trackHeight = paddingBox.height - barPadding * 2;

        barThumbBox.height = GMath.clamp(
            contentHeight > 0 ? visibleHeight * visibleHeight / contentHeight : visibleHeight,
            Math.min(visibleHeight, 10), visibleHeight
        );

        float oldOffsetY = scrollOffsetY;
        float clamped = clampScrollOffsetY(scrollOffsetY);
        if (oldOffsetY != clamped)
            barThumbOffsetYAnim.animateIfDifferent(clamped, 150, Easing.IN_SINE);
    }

    @Override
    protected void readYogaLayout() {
        super.readYogaLayout();
        hasOverflow = Yoga.YGNodeLayoutGetHadOverflow(yogaNode);
    }

    @Override
    protected boolean handleMouseScroll(float mouseX, float mouseY, float amount, float vertical) {
        if (vertical != 0 && hasOverflow) {
            float newOffsetY = clampScrollOffsetY(barThumbOffsetYAnim.getFinal() - vertical * 20);
            barThumbOffsetYAnim.animateIfDifferent(clampScrollOffsetY(newOffsetY), 150, Easing.IN_SINE);
            return true;
        }
        return super.handleMouseScroll(mouseX, mouseY, amount, vertical);
    }

    @Override
    protected void handleMouseMoved(float mouseX, float mouseY, boolean canHover) {
        if (thumbDragged) {
            float newOffsetY = (mouseY - dragStartY) * (contentHeight - visibleHeight) / (trackHeight - barThumbBox.height);
            barThumbOffsetYAnim.set(clampScrollOffsetY(newOffsetY));
            return;
        }
        thumbHovered = canHover && hasOverflow && barThumbBox.contains(mouseX, mouseY);
        canHover = canHover && borderBox.contains(mouseX, mouseY);
        super.handleMouseMoved(mouseX, mouseY, canHover);
    }

    @Override
    protected boolean handleMousePress(float mouseX, float mouseY, int button) {
        if (thumbHovered) {
            thumbDragged = true;
            dragStartY = mouseY - barThumbOffsetY;
            return true;
        }
        return super.handleMousePress(mouseX, mouseY, button);
    }

    @Override
    protected boolean handleMouseRelease(float mouseX, float mouseY, int button, boolean blocked) {
        if (thumbDragged) {
            thumbDragged = false;
            blocked = true;
        }
        return super.handleMouseRelease(mouseX, mouseY, button, blocked);
    }

    private float clampScrollOffsetY(float offset) {
        return GMath.clamp(offset, 0, Math.max(0, contentHeight - visibleHeight));
    }

    private void updateScrollOffsetFromAnim(float value) {
        if (scrollOffsetY == value)
            return;
        scrollOffsetY = value;

        float availableBarHeight = trackHeight - barThumbBox.height;
        barThumbOffsetY = GMath.clamp(
            contentHeight > visibleHeight ? availableBarHeight * scrollOffsetY / (contentHeight - visibleHeight) : 0,
            0, availableBarHeight
        );
        barThumbBox.x = paddingBox.x + paddingBox.width - barThumbBox.width - barPadding;
        barThumbBox.y = paddingBox.y + barThumbOffsetY + barPadding;

        modifyContentBoxes();
    }

    private void modifyContentBoxes() {
        content.borderBox.y = content.origBorderBoxY - scrollOffsetY;
        content.paddingBox.y = content.origPaddingBoxY - scrollOffsetY;
        content.contentBox.y = content.origContentBoxY - scrollOffsetY;
    }

    @Override
    public void tick() {
        super.tick();

        int thumbColor = barThumbColor;
        if (thumbDragged)
            thumbColor = barThumbActiveColor;
        else if (thumbHovered)
            thumbColor = barThumbHoverColor;
        barThumbColorAnim.animateIfDifferent(thumbColor, 100, Easing.EASE);
        modifyContentBoxes();
    }

    @Override
    public void draw(Context ctx) {
        if (hasOverflow)
            if (!ctx.pushScissor(Bounds.fromBox(borderBox)))
                return;

        super.draw(ctx);

        drawScrollbars(ctx);

        if (hasOverflow)
            ctx.popScissor();
    }

    protected void drawScrollbars(Context ctx) {
        if (hasOverflow)
            drawDefaultVerticalScrollbar(ctx);
    }

    protected void drawDefaultVerticalScrollbar(Context ctx) {
        ctx.drawRect(
            barThumbBox.x - barPadding,
            paddingBox.y,
            barThumbBox.width + barPadding * 2,
            paddingBox.height,
            barColor
        );
        ctx.drawRect(barThumbBox, new BorderRadius(3), barThumbColorComputed);
    }

    private static class ScrollableContent extends Element {
        float origBorderBoxY;
        float origPaddingBoxY;
        float origContentBoxY;

        @Override
        protected void readYogaLayout() {
            super.readYogaLayout();
            origBorderBoxY = borderBox.y;
            origPaddingBoxY = paddingBox.y;
            origContentBoxY = contentBox.y;
        }
    }
}
