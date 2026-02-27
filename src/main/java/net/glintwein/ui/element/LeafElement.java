package net.glintwein.ui.element;

import net.glintwein.ui.data.Size;
import net.glintwein.ui.util.FloatSupplier;
import org.lwjgl.util.yoga.Yoga;

import java.util.Collections;
import java.util.List;

public class LeafElement extends Element {
    protected void setMeasureFunctionAspectRatio(FloatSupplier widthGetter, FloatSupplier heightGetter) {
        setMeasureFunction((width, widthMode, height, heightMode) -> {
            float ownWidth = widthGetter.get();
            float ownHeight = heightGetter.get();
            float aspectRatio = ownWidth / ownHeight;
            if (widthMode == SizeMode.EXACTLY) {
                float measuredHeight = width / aspectRatio;
                return new Size(width, measuredHeight);
            } else if (heightMode == SizeMode.EXACTLY) {
                float measuredWidth = height * aspectRatio;
                return new Size(measuredWidth, height);
            } else if (widthMode == SizeMode.AT_MOST && heightMode == SizeMode.AT_MOST) {
                float maxWidth = width;
                float maxHeight = height;
                float widthBasedHeight = maxWidth / aspectRatio;
                if (widthBasedHeight <= maxHeight) {
                    return new Size(maxWidth, widthBasedHeight);
                } else {
                    float heightBasedWidth = maxHeight * aspectRatio;
                    return new Size(heightBasedWidth, maxHeight);
                }
            } else if (widthMode == SizeMode.AT_MOST) {
                float measuredHeight = Math.min(width, ownWidth) / aspectRatio;
                return new Size(width, measuredHeight);
            } else if (heightMode == SizeMode.AT_MOST) {
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
        Yoga.YGNodeMarkDirty(yogaNode);
    }
}
