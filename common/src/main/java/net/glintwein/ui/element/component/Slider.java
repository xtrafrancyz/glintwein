package net.glintwein.ui.element.component;

import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Gradient;
import net.glintwein.ui.data.Size;
import net.glintwein.ui.element.LeafElement;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawRectBuilder;
import net.glintwein.ui.util.ARGB;
import net.glintwein.ui.util.FloatConsumer;
import net.glintwein.ui.util.GMath;

public class Slider extends LeafElement {
    private float value;
    private float minValue;
    private float maxValue;
    private FloatConsumer onValueChanged;
    private float thumbSize = 16;
    private float trackHeight = 8;

    public Slider(float minValue, float maxValue, float initialValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        setMinHeight(thumbSize);
        setValue(initialValue);
        setMeasureFunction((width, widthMode, height, heightMode) -> {
            return new Size(width, thumbSize);
        });
        setOnMousePress((x, y, button) -> {
            setByMouse(x);
            return true;
        });
    }

    public void setOnValueChanged(FloatConsumer listener) {
        this.onValueChanged = listener;
    }

    public void setValue(float value) {
        this.value = GMath.clamp(value, minValue, maxValue);
    }

    public float getValue() {
        return value;
    }

    public void setThumbSize(float thumbSize) {
        if (thumbSize != this.thumbSize)
            return;
        this.thumbSize = thumbSize;
        setMinHeight(thumbSize);
        markDirty();
    }

    public void setTrackHeight(float trackHeight) {
        this.trackHeight = trackHeight;
    }

    @Override
    protected void handleMouseMoved(float mouseX, float mouseY, boolean canHover) {
        super.handleMouseMoved(mouseX, mouseY, canHover);
        if (isPressed())
            setByMouse(mouseX - contentBox.x);
    }

    private void setByMouse(float x) {
        float relativeX = x - thumbSize / 2; // Center the thumb on the mouse position
        float newValue = minValue + (relativeX / (contentBox.width - thumbSize)) * (maxValue - minValue);
        setValue(newValue);
        if (onValueChanged != null) {
            onValueChanged.accept(value);
        }
    }

    @Override
    protected void drawContent(Context ctx) {
        BorderRadius radius = BorderRadius.of(trackHeight / 2);
        float trackY = contentBox.y + (contentBox.height - trackHeight) / 2;
        // background track
        ctx.drawRect(borderBox.x, trackY, contentBox.width, trackHeight, radius, 0xFF1C1E44);
        // filled track
        float filledWidth = ((value - minValue) / (maxValue - minValue)) * (contentBox.width - thumbSize) + thumbSize / 2;
        int leftColor = 0xFF4557DF;
        int rightColor = 0xFF54a0C9;
        ctx.drawRect(DrawRectBuilder.fromXYWH(contentBox.x, trackY, filledWidth, trackHeight)
            .radius(radius)
            .color(Gradient.leftToRight(leftColor, ARGB.lerp(filledWidth / contentBox.width, leftColor, rightColor)))
            .outline(0xFF4A82BE, GlobalUIState.minimumOnePixel())
        );

        float thumbX = contentBox.x + ((value - minValue) / (maxValue - minValue)) * (contentBox.width - thumbSize);
        float thumbY = contentBox.y + (contentBox.height - thumbSize) / 2;
        ctx.drawRect(DrawRectBuilder.fromXYWH(thumbX, thumbY, thumbSize, thumbSize)
            .radius(BorderRadius.of(thumbSize))
            .color(0xFFFFFFFF)
        );
    }
}
