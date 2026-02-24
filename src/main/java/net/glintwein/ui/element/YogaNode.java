package net.glintwein.ui.element;

import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.*;
import net.glintwein.ui.util.NativeCleaner;
import org.lwjgl.util.yoga.YGMeasureFunc;
import org.lwjgl.util.yoga.Yoga;

import java.util.ArrayList;
import java.util.List;

public abstract class YogaNode {
    protected final long yogaNode;
    private final List<NativeCleaner.Handle> cleanerHandlers = new ArrayList<>();
    private Display display = Display.FLEX;

    public YogaNode() {
        this.yogaNode = Yoga.YGNodeNewWithConfig(GlobalUIState.getYogaConfigHandle());

        long yogaNodeFinal = this.yogaNode;
        cleanerHandlers.add(NativeCleaner.register(() -> Yoga.YGNodeFree(yogaNodeFinal)));
    }

    public Display getDisplayType() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
        Yoga.YGNodeStyleSetDisplay(yogaNode, display.getValue());
    }

    public void setAspectRatio(float width, float height) {
        if (height == 0) {
            throw new IllegalArgumentException("Height cannot be zero when setting aspect ratio.");
        }
        Yoga.YGNodeStyleSetAspectRatio(yogaNode, width / height);
    }

    public void setAlignContent(Align align) {
        Yoga.YGNodeStyleSetAlignContent(yogaNode, align.getValue());
    }

    public void setAlignItems(Align align) {
        Yoga.YGNodeStyleSetAlignItems(yogaNode, align.getValue());
    }

    public void setAlignSelf(Align align) {
        Yoga.YGNodeStyleSetAlignSelf(yogaNode, align.getValue());
    }

    public void setJustifyContent(Justify justify) {
        Yoga.YGNodeStyleSetJustifyContent(yogaNode, justify.getValue());
    }

    public void setBorder(Edge edge, float width) {
        Yoga.YGNodeStyleSetBorder(yogaNode, edge.getValue(), width);
    }

    public void setFlexGrow(float flexGrow) {
        Yoga.YGNodeStyleSetFlexGrow(yogaNode, flexGrow);
    }

    public void setFlexShrink(float flexShrink) {
        Yoga.YGNodeStyleSetFlexShrink(yogaNode, flexShrink);
    }

    public void setFlex(float flex) {
        Yoga.YGNodeStyleSetFlex(yogaNode, flex);
    }

    public void setFlexBasisPercent(float percent) {
        Yoga.YGNodeStyleSetFlexBasisPercent(yogaNode, percent);
    }

    public void setFlexBasisAuto() {
        Yoga.YGNodeStyleSetFlexBasisAuto(yogaNode);
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
        Yoga.YGNodeStyleSetWidth(yogaNode, width);
    }

    public void setWidthPercent(float percent) {
        Yoga.YGNodeStyleSetWidthPercent(yogaNode, percent);
    }

    public void setWidthAuto() {
        Yoga.YGNodeStyleSetWidthAuto(yogaNode);
    }

    public void setMinWidth(float minWidth) {
        Yoga.YGNodeStyleSetMinWidth(yogaNode, minWidth);
    }

    public void setMaxWidth(float maxWidth) {
        Yoga.YGNodeStyleSetMaxWidth(yogaNode, maxWidth);
    }

    public void setMinWidthPercent(float percent) {
        Yoga.YGNodeStyleSetMinWidthPercent(yogaNode, percent);
    }

    public void setMaxWidthPercent(float percent) {
        Yoga.YGNodeStyleSetMaxWidthPercent(yogaNode, percent);
    }

    public void setHeight(float height) {
        Yoga.YGNodeStyleSetHeight(yogaNode, height);
    }

    public void setHeightPercent(float percent) {
        Yoga.YGNodeStyleSetHeightPercent(yogaNode, percent);
    }

    public void setHeightAuto() {
        Yoga.YGNodeStyleSetHeightAuto(yogaNode);
    }

    public void setMinHeight(float minHeight) {
        Yoga.YGNodeStyleSetMinHeight(yogaNode, minHeight);
    }

    public void setMaxHeight(float maxHeight) {
        Yoga.YGNodeStyleSetMaxHeight(yogaNode, maxHeight);
    }

    public void setMinHeightPercent(float percent) {
        Yoga.YGNodeStyleSetMinHeightPercent(yogaNode, percent);
    }

    public void setMaxHeightPercent(float percent) {
        Yoga.YGNodeStyleSetMaxHeightPercent(yogaNode, percent);
    }

    public void setAspectRatio(float aspectRatio) {
        Yoga.YGNodeStyleSetAspectRatio(yogaNode, aspectRatio);
    }

    public void setPositionType(PositionType positionType) {
        Yoga.YGNodeStyleSetPositionType(yogaNode, positionType.getValue());
    }

    public void setPosition(Edge edge, float position) {
        Yoga.YGNodeStyleSetPosition(yogaNode, edge.getValue(), position);
    }

    public void setPositionPercent(Edge edge, float percent) {
        Yoga.YGNodeStyleSetPositionPercent(yogaNode, edge.getValue(), percent);
    }

    public void setFlexDirection(FlexDirection direction) {
        Yoga.YGNodeStyleSetFlexDirection(yogaNode, direction.getValue());
    }

    public void setMargin(Edge edge, float margin) {
        Yoga.YGNodeStyleSetMargin(yogaNode, edge.getValue(), margin);
    }

    public void setMarginPercent(Edge edge, float percent) {
        Yoga.YGNodeStyleSetMarginPercent(yogaNode, edge.getValue(), percent);
    }

    public void setMarginAuto(Edge edge) {
        Yoga.YGNodeStyleSetMarginAuto(yogaNode, edge.getValue());
    }

    public void setPadding(Edge edge, float padding) {
        Yoga.YGNodeStyleSetPadding(yogaNode, edge.getValue(), padding);
    }

    public void setPaddingPercent(Edge edge, float percent) {
        Yoga.YGNodeStyleSetPaddingPercent(yogaNode, edge.getValue(), percent);
    }

    public void setOverflow(Overflow overflow) {
        Yoga.YGNodeStyleSetOverflow(yogaNode, overflow.getValue());
    }

    public void setWrap(Wrap wrap) {
        Yoga.YGNodeStyleSetFlexWrap(yogaNode, wrap.getValue());
    }

    protected void setMeasureFunction(MeasureFunction measureFunc) {
        YGMeasureFunc nativeMeasureFunc = YGMeasureFunc.create((node, width, widthMode, height, heightMode) -> {
            Size result = measureFunc.measure(width, SizeMode.fromYoga(widthMode), height, SizeMode.fromYoga(heightMode));
            return Float.floatToRawIntBits(result.getWidth()) | ((long) Float.floatToRawIntBits(result.getHeight()) << 32);
        });
        cleanerHandlers.add(NativeCleaner.register(nativeMeasureFunc::free));
        Yoga.YGNodeSetMeasureFunc(yogaNode, nativeMeasureFunc);
    }

    public static long getYogaNodeHandle(YogaNode node) {
        return node != null ? node.yogaNode : 0;
    }

    protected interface MeasureFunction {
        Size measure(float width, SizeMode widthMode, float height, SizeMode heightMode);
    }

    protected enum SizeMode {
        EXACTLY,
        AT_MOST,
        UNDEFINED;

        static SizeMode fromYoga(int yogaMode) {
            switch (yogaMode) {
                case Yoga.YGMeasureModeExactly:
                    return EXACTLY;
                case Yoga.YGMeasureModeAtMost:
                    return AT_MOST;
                case Yoga.YGMeasureModeUndefined:
                    return UNDEFINED;
                default:
                    throw new IllegalArgumentException("Unknown Yoga size mode: " + yogaMode);
            }
        }
    }
}
