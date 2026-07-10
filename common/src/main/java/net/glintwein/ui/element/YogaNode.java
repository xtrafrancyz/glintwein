package net.glintwein.ui.element;

import net.glintwein.platform.Platform;
import net.glintwein.platform.YogaMeasureFunction;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.*;
import net.glintwein.ui.util.NativeCleaner;
import org.lwjgl.system.NativeResource;

import java.lang.ref.WeakReference;

public abstract class YogaNode {
    protected final long yogaNode;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Display display = Display.FLEX;
    private Overflow overflow = Overflow.VISIBLE;
    private YogaMeasureFunction measureFunction;

    public YogaNode() {
        this.yogaNode = Platform.yoga().NodeNewWithConfig(GlobalUIState.getYogaConfigHandle());

        long yogaNodeFinal = this.yogaNode;
        NativeCleaner.register(this, () -> Platform.yoga().NodeFree(yogaNodeFinal));
    }

    public Display getDisplayType() {
        return display;
    }

    public void setDisplay(Display display) {
        if (this.display == display)
            return;
        this.display = display;
        Platform.yoga().NodeStyleSetDisplay(yogaNode, display);
    }

    public void setAspectRatio(float width, float height) {
        if (height == 0) {
            throw new IllegalArgumentException("Height cannot be zero when setting aspect ratio.");
        }
        Platform.yoga().NodeStyleSetAspectRatio(yogaNode, width / height);
    }

    public void setAlignContent(Align align) {
        Platform.yoga().NodeStyleSetAlignContent(yogaNode, align);
    }

    public void setAlignItems(Align align) {
        Platform.yoga().NodeStyleSetAlignItems(yogaNode, align);
    }

    public void setAlignSelf(Align align) {
        Platform.yoga().NodeStyleSetAlignSelf(yogaNode, align);
    }

    public void setJustifyContent(Justify justify) {
        Platform.yoga().NodeStyleSetJustifyContent(yogaNode, justify);
    }

    public void setBorder(Edge edge, float width) {
        Platform.yoga().NodeStyleSetBorder(yogaNode, edge, width);
    }

    public void setFlexGrow(float flexGrow) {
        Platform.yoga().NodeStyleSetFlexGrow(yogaNode, flexGrow);
    }

    public void setFlexShrink(float flexShrink) {
        Platform.yoga().NodeStyleSetFlexShrink(yogaNode, flexShrink);
    }

    public void setFlex(float flex) {
        Platform.yoga().NodeStyleSetFlex(yogaNode, flex);
    }

    public void setFlexBasis(float flexBasis) {
        Platform.yoga().NodeStyleSetFlexBasis(yogaNode, flexBasis);
    }

    public void setFlexBasisPercent(float percent) {
        Platform.yoga().NodeStyleSetFlexBasisPercent(yogaNode, percent);
    }

    public void setFlexBasisAuto() {
        Platform.yoga().NodeStyleSetFlexBasisAuto(yogaNode);
    }

    public void setSize(float size) {
        setWidth(size);
        setHeight(size);
    }

    public void setSize(float width, float height) {
        setWidth(width);
        setHeight(height);
    }

    public void setSizePercent(float percent) {
        setWidthPercent(percent);
        setHeightPercent(percent);
    }

    public void setSizePercent(float widthPercent, float heightPercent) {
        setWidthPercent(widthPercent);
        setHeightPercent(heightPercent);
    }

    public void setWidth(float width) {
        Platform.yoga().NodeStyleSetWidth(yogaNode, width);
    }

    public void setWidthPercent(float percent) {
        Platform.yoga().NodeStyleSetWidthPercent(yogaNode, percent);
    }

    public void setWidthAuto() {
        Platform.yoga().NodeStyleSetWidthAuto(yogaNode);
    }

    public void setMinWidth(float minWidth) {
        Platform.yoga().NodeStyleSetMinWidth(yogaNode, minWidth);
    }

    public void setMaxWidth(float maxWidth) {
        Platform.yoga().NodeStyleSetMaxWidth(yogaNode, maxWidth);
    }

    public void setMinWidthPercent(float percent) {
        Platform.yoga().NodeStyleSetMinWidthPercent(yogaNode, percent);
    }

    public void setMaxWidthPercent(float percent) {
        Platform.yoga().NodeStyleSetMaxWidthPercent(yogaNode, percent);
    }

    public void setHeight(float height) {
        Platform.yoga().NodeStyleSetHeight(yogaNode, height);
    }

    public void setHeightPercent(float percent) {
        Platform.yoga().NodeStyleSetHeightPercent(yogaNode, percent);
    }

    public void setHeightAuto() {
        Platform.yoga().NodeStyleSetHeightAuto(yogaNode);
    }

    public void setMinHeight(float minHeight) {
        Platform.yoga().NodeStyleSetMinHeight(yogaNode, minHeight);
    }

    public void setMaxHeight(float maxHeight) {
        Platform.yoga().NodeStyleSetMaxHeight(yogaNode, maxHeight);
    }

    public void setMinHeightPercent(float percent) {
        Platform.yoga().NodeStyleSetMinHeightPercent(yogaNode, percent);
    }

    public void setMaxHeightPercent(float percent) {
        Platform.yoga().NodeStyleSetMaxHeightPercent(yogaNode, percent);
    }

    public void setAspectRatio(float aspectRatio) {
        Platform.yoga().NodeStyleSetAspectRatio(yogaNode, aspectRatio);
    }

    public void setPositionType(PositionType positionType) {
        Platform.yoga().NodeStyleSetPositionType(yogaNode, positionType);
    }

    public void setPosition(Edge edge, float position) {
        Platform.yoga().NodeStyleSetPosition(yogaNode, edge, position);
    }

    public void setPositionPercent(Edge edge, float percent) {
        Platform.yoga().NodeStyleSetPositionPercent(yogaNode, edge, percent);
    }

    public void setFlexDirection(FlexDirection direction) {
        Platform.yoga().NodeStyleSetFlexDirection(yogaNode, direction);
    }

    public void setMargin(Edge edge, float margin) {
        Platform.yoga().NodeStyleSetMargin(yogaNode, edge, margin);
    }

    public void setMarginPercent(Edge edge, float percent) {
        Platform.yoga().NodeStyleSetMarginPercent(yogaNode, edge, percent);
    }

    public void setMarginAuto(Edge edge) {
        Platform.yoga().NodeStyleSetMarginAuto(yogaNode, edge);
    }

    public void setPadding(Edge edge, float padding) {
        Platform.yoga().NodeStyleSetPadding(yogaNode, edge, padding);
    }

    public void setPaddingPercent(Edge edge, float percent) {
        Platform.yoga().NodeStyleSetPaddingPercent(yogaNode, edge, percent);
    }

    public Overflow getOverflow() {
        return overflow;
    }

    public void setOverflow(Overflow overflow) {
        if (this.overflow == overflow)
            return;
        this.overflow = overflow;
        Platform.yoga().NodeStyleSetOverflow(yogaNode, overflow);
    }

    public void setWrap(Wrap wrap) {
        Platform.yoga().NodeStyleSetFlexWrap(yogaNode, wrap);
    }

    protected void setMeasureFunction(YogaMeasureFunction measureFunc) {
        if (this.measureFunction != null)
            throw new IllegalStateException("Measure function is already set. It can only be set once.");
        measureFunction = measureFunc;

        // decouple lambda from YogaNode to avoid memory leaks.
        // The lambda will hold a strong reference to the YogaNode, preventing it from being garbage collected.
        // Using a WeakReference allows the YogaNode to be collected if there are no other strong references to it.
        WeakReference<YogaNode> weakNodeRef = new WeakReference<>(this);
        YogaMeasureFunction nativeMeasureFunc = (width, widthMode, height, heightMode) -> {
            YogaNode yogaNode = weakNodeRef.get();
            if (yogaNode == null)
                return new Size(0, 0);
            return yogaNode.measureFunction.measure(width, widthMode, height, heightMode);
        };

        NativeResource nativeResource = Platform.yoga().NodeSetMeasureFunc(yogaNode, nativeMeasureFunc);
        NativeCleaner.register(this, nativeResource::free);
    }

    public static long getYogaNodeHandle(YogaNode node) {
        return node != null ? node.yogaNode : 0;
    }
}
