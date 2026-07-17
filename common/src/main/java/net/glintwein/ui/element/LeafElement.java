package net.glintwein.ui.element;

import net.glintwein.platform.Platform;
import net.glintwein.platform.YogaMeasureFunction;
import net.glintwein.ui.data.Size;
import net.glintwein.ui.util.FloatSupplier;

import java.util.Collections;
import java.util.List;

public class LeafElement extends Element {
    protected void setMeasureFunctionAspectRatio(FloatSupplier widthGetter, FloatSupplier heightGetter) {
        setMeasureFunction((width, widthMode, height, heightMode) -> {
            float ownWidth = widthGetter.get();
            float ownHeight = heightGetter.get();
            if (ownWidth <= 0 || ownHeight <= 0)
                return new Size(0, 0);
            float aspectRatio = ownWidth / ownHeight;
            if (widthMode == YogaMeasureFunction.SizeMode.EXACTLY) {
                float measuredHeight = width / aspectRatio;
                return new Size(width, measuredHeight);
            } else if (heightMode == YogaMeasureFunction.SizeMode.EXACTLY) {
                float measuredWidth = height * aspectRatio;
                return new Size(measuredWidth, height);
            } else if (widthMode == YogaMeasureFunction.SizeMode.AT_MOST && heightMode == YogaMeasureFunction.SizeMode.AT_MOST) {
                float maxWidth = width;
                float maxHeight = height;
                float widthBasedHeight = maxWidth / aspectRatio;
                if (widthBasedHeight <= maxHeight) {
                    return new Size(maxWidth, widthBasedHeight);
                } else {
                    float heightBasedWidth = maxHeight * aspectRatio;
                    return new Size(heightBasedWidth, maxHeight);
                }
            } else if (widthMode == YogaMeasureFunction.SizeMode.AT_MOST) {
                float measuredHeight = Math.min(width, ownWidth) / aspectRatio;
                return new Size(width, measuredHeight);
            } else if (heightMode == YogaMeasureFunction.SizeMode.AT_MOST) {
                float measuredWidth = Math.min(height, ownHeight) * aspectRatio;
                return new Size(measuredWidth, height);
            }
            return new Size(ownWidth, ownHeight);
        });
    }

    @Override
    @Deprecated
    public List<Element> getChildren() {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public void addChild(Element child) {
        throw new UnsupportedOperationException("LeafElement cannot have children");
    }

    @Override
    @Deprecated
    public void addChild(Element child, int index) {
        throw new UnsupportedOperationException("LeafElement cannot have children");
    }

    @Override
    @Deprecated
    public void removeChild(Element child) {
        throw new UnsupportedOperationException("LeafElement cannot have children");
    }

    @Override
    @Deprecated
    public void clearChildren() {
        // No-op
    }

    protected void markDirty() {
        Platform.yoga().NodeMarkDirty(yogaNode);
    }
}
