package net.glintwein.platform;

import net.glintwein.ui.data.*;
import org.lwjgl.system.NativeResource;
import org.lwjgl.util.yoga.YGMeasureFunc;
import org.lwjgl.util.yoga.Yoga;

public class Yoga3_2_2 implements YogaShim {
    @Override
    public long ConfigNew() {
        return Yoga.YGConfigNew();
    }

    @Override
    public void ConfigSetPointScaleFactor(long config, float pixelsInPoint) {
        Yoga.YGConfigSetPointScaleFactor(config, pixelsInPoint);
    }

    @Override
    public long NodeNewWithConfig(long config) {
        return Yoga.YGNodeNewWithConfig(config);
    }

    @Override
    public void NodeFree(long node) {
        Yoga.YGNodeFree(node);
    }

    @Override
    public void NodeStyleSetDisplay(long node, Display display) {
        Yoga.YGNodeStyleSetDisplay(node, displayValue(display));
    }

    @Override
    public void NodeStyleSetAspectRatio(long node, float aspectRatio) {
        Yoga.YGNodeStyleSetAspectRatio(node, aspectRatio);
    }

    @Override
    public void NodeStyleSetAlignContent(long node, Align align) {
        Yoga.YGNodeStyleSetAlignContent(node, alignValue(align));
    }

    @Override
    public void NodeStyleSetAlignItems(long node, Align align) {
        Yoga.YGNodeStyleSetAlignItems(node, alignValue(align));
    }

    @Override
    public void NodeStyleSetAlignSelf(long node, Align align) {
        Yoga.YGNodeStyleSetAlignSelf(node, alignValue(align));
    }

    @Override
    public void NodeStyleSetJustifyContent(long node, Justify justify) {
        Yoga.YGNodeStyleSetJustifyContent(node, justifyValue(justify));
    }

    @Override
    public void NodeStyleSetBorder(long node, Edge edge, float width) {
        Yoga.YGNodeStyleSetBorder(node, edgeValue(edge), width);
    }

    @Override
    public void NodeStyleSetFlex(long node, float flex) {
        Yoga.YGNodeStyleSetFlex(node, flex);
    }

    @Override
    public void NodeStyleSetFlexGrow(long node, float flexGrow) {
        Yoga.YGNodeStyleSetFlexGrow(node, flexGrow);
    }

    @Override
    public void NodeStyleSetFlexShrink(long node, float flexShrink) {
        Yoga.YGNodeStyleSetFlexShrink(node, flexShrink);
    }

    @Override
    public void NodeStyleSetFlexBasis(long node, float flexBasis) {
        Yoga.YGNodeStyleSetFlexBasis(node, flexBasis);
    }

    @Override
    public void NodeStyleSetFlexBasisPercent(long node, float percent) {
        Yoga.YGNodeStyleSetFlexBasisPercent(node, percent);
    }

    @Override
    public void NodeStyleSetFlexBasisAuto(long node) {
        Yoga.YGNodeStyleSetFlexBasisAuto(node);
    }

    @Override
    public void NodeStyleSetFlexDirection(long node, FlexDirection flexDirection) {
        Yoga.YGNodeStyleSetFlexDirection(node, flexDirectionValue(flexDirection));
    }

    @Override
    public void NodeStyleSetFlexWrap(long node, Wrap wrap) {
        Yoga.YGNodeStyleSetFlexWrap(node, wrapValue(wrap));
    }

    @Override
    public void NodeStyleSetWidth(long node, float width) {
        Yoga.YGNodeStyleSetWidth(node, width);
    }

    @Override
    public void NodeStyleSetHeight(long node, float height) {
        Yoga.YGNodeStyleSetHeight(node, height);
    }

    @Override
    public void NodeStyleSetWidthPercent(long node, float percent) {
        Yoga.YGNodeStyleSetWidthPercent(node, percent);
    }

    @Override
    public void NodeStyleSetHeightPercent(long node, float percent) {
        Yoga.YGNodeStyleSetHeightPercent(node, percent);
    }

    @Override
    public void NodeStyleSetWidthAuto(long node) {
        Yoga.YGNodeStyleSetWidthAuto(node);
    }

    @Override
    public void NodeStyleSetHeightAuto(long node) {
        Yoga.YGNodeStyleSetHeightAuto(node);
    }

    @Override
    public void NodeStyleSetMinWidth(long node, float minWidth) {
        Yoga.YGNodeStyleSetMinWidth(node, minWidth);
    }

    @Override
    public void NodeStyleSetMinHeight(long node, float minHeight) {
        Yoga.YGNodeStyleSetMinHeight(node, minHeight);
    }

    @Override
    public void NodeStyleSetMinWidthPercent(long node, float percent) {
        Yoga.YGNodeStyleSetMinWidthPercent(node, percent);
    }

    @Override
    public void NodeStyleSetMinHeightPercent(long node, float percent) {
        Yoga.YGNodeStyleSetMinHeightPercent(node, percent);
    }

    @Override
    public void NodeStyleSetMaxWidth(long node, float maxWidth) {
        Yoga.YGNodeStyleSetMaxWidth(node, maxWidth);
    }

    @Override
    public void NodeStyleSetMaxHeight(long node, float maxHeight) {
        Yoga.YGNodeStyleSetMaxHeight(node, maxHeight);
    }

    @Override
    public void NodeStyleSetMaxWidthPercent(long node, float percent) {
        Yoga.YGNodeStyleSetMaxWidthPercent(node, percent);
    }

    @Override
    public void NodeStyleSetMaxHeightPercent(long node, float percent) {
        Yoga.YGNodeStyleSetMaxHeightPercent(node, percent);
    }

    @Override
    public void NodeStyleSetPositionType(long node, PositionType positionType) {
        Yoga.YGNodeStyleSetPositionType(node, positionTypeValue(positionType));
    }

    @Override
    public void NodeStyleSetPosition(long node, Edge edge, float position) {
        Yoga.YGNodeStyleSetPosition(node, edgeValue(edge), position);
    }

    @Override
    public void NodeStyleSetPositionPercent(long node, Edge edge, float percent) {
        Yoga.YGNodeStyleSetPositionPercent(node, edgeValue(edge), percent);
    }

    @Override
    public void NodeStyleSetMargin(long node, Edge edge, float margin) {
        Yoga.YGNodeStyleSetMargin(node, edgeValue(edge), margin);
    }

    @Override
    public void NodeStyleSetMarginPercent(long node, Edge edge, float percent) {
        Yoga.YGNodeStyleSetMarginPercent(node, edgeValue(edge), percent);
    }

    @Override
    public void NodeStyleSetMarginAuto(long node, Edge edge) {
        Yoga.YGNodeStyleSetMarginAuto(node, edgeValue(edge));
    }

    @Override
    public void NodeStyleSetPadding(long node, Edge edge, float padding) {
        Yoga.YGNodeStyleSetPadding(node, edgeValue(edge), padding);
    }

    @Override
    public void NodeStyleSetPaddingPercent(long node, Edge edge, float percent) {
        Yoga.YGNodeStyleSetPaddingPercent(node, edgeValue(edge), percent);
    }

    @Override
    public void NodeStyleSetOverflow(long node, Overflow overflow) {
        Yoga.YGNodeStyleSetOverflow(node, overflowValue(overflow));
    }

    @Override
    public void NodeInsertChild(long node, long child, int index) {
        Yoga.YGNodeInsertChild(node, child, index);
    }

    @Override
    public void NodeRemoveChild(long node, long child) {
        Yoga.YGNodeRemoveChild(node, child);
    }

    @Override
    public void NodeRemoveAllChildren(long node) {
        Yoga.YGNodeRemoveAllChildren(node);
    }

    @Override
    public boolean NodeGetHasNewLayout(long node) {
        return Yoga.YGNodeGetHasNewLayout(node);
    }

    @Override
    public void NodeSetHasNewLayout(long node, boolean hasNewLayout) {
        Yoga.YGNodeSetHasNewLayout(node, hasNewLayout);
    }

    private final YogaLayoutResult layoutResult = new YogaLayoutResult();

    @Override
    public YogaLayoutResult NodeGetLayout(long node) {
        YogaLayoutResult r = layoutResult;

        // The Border Box (Outer dimensions)
        r.border.x = Yoga.YGNodeLayoutGetLeft(node);
        r.border.y = Yoga.YGNodeLayoutGetTop(node);
        r.border.width = Yoga.YGNodeLayoutGetWidth(node);
        r.border.height = Yoga.YGNodeLayoutGetHeight(node);

        // The Padding Box
        float borderLeft = Yoga.YGNodeLayoutGetBorder(node, Yoga.YGEdgeLeft);
        float borderTop = Yoga.YGNodeLayoutGetBorder(node, Yoga.YGEdgeTop);
        r.padding.x = r.border.x + borderLeft;
        r.padding.y = r.border.y + borderTop;
        r.padding.width = r.border.width - borderLeft - Yoga.YGNodeLayoutGetBorder(node, Yoga.YGEdgeRight);
        r.padding.height = r.border.height - borderTop - Yoga.YGNodeLayoutGetBorder(node, Yoga.YGEdgeBottom);

        // The Content Box
        float paddingLeft = Yoga.YGNodeLayoutGetPadding(node, Yoga.YGEdgeLeft);
        float paddingTop = Yoga.YGNodeLayoutGetPadding(node, Yoga.YGEdgeTop);
        r.content.x = r.padding.x + paddingLeft;
        r.content.y = r.padding.y + paddingTop;
        r.content.width = r.padding.width - paddingLeft - Yoga.YGNodeLayoutGetPadding(node, Yoga.YGEdgeRight);
        r.content.height = r.padding.height - paddingTop - Yoga.YGNodeLayoutGetPadding(node, Yoga.YGEdgeBottom);

        return r;
    }

    @Override
    public boolean NodeLayoutGetHadOverflow(long node) {
        return Yoga.YGNodeLayoutGetHadOverflow(node);
    }

    @Override
    public void NodeMarkDirty(long node) {
        Yoga.YGNodeMarkDirty(node);
    }

    @Override
    public boolean NodeIsDirty(long node) {
        return Yoga.YGNodeIsDirty(node);
    }

    @Override
    public void NodeCalculateLayout(long node, float availableWidth, float availableHeight) {
        Yoga.YGNodeCalculateLayout(node, availableWidth, availableHeight, Yoga.YGDirectionLTR);
    }

    @Override
    public void NodeMarkDirtyAndPropogateToDescendants(long node) {
        Yoga.nYGNodeMarkDirtyAndPropogateToDescendants(node);
    }

    @Override
    public NativeResource NodeSetMeasureFunc(long node, YogaMeasureFunction measureFunc) {
        YGMeasureFunc nativeMeasureFunc = YGMeasureFunc.create((node0, width, widthMode, height, heightMode) -> {
            Size result = measureFunc.measure(width, sizeModeFromYoga(widthMode), height, sizeModeFromYoga(heightMode));
            return Float.floatToRawIntBits(result.getWidth()) | ((long) Float.floatToRawIntBits(result.getHeight()) << 32);
        });
        Yoga.YGNodeSetMeasureFunc(node, nativeMeasureFunc);
        return nativeMeasureFunc;
    }

    private static int displayValue(Display display) {
        switch (display) {
            case FLEX:
                return Yoga.YGDisplayFlex;
            case NONE:
                return Yoga.YGDisplayNone;
            default:
                throw new IllegalArgumentException("Unknown display: " + display);
        }
    }

    private static int alignValue(Align align) {
        switch (align) {
            case AUTO:
                return Yoga.YGAlignAuto;
            case FLEX_START:
                return Yoga.YGAlignFlexStart;
            case CENTER:
                return Yoga.YGAlignCenter;
            case FLEX_END:
                return Yoga.YGAlignFlexEnd;
            case STRETCH:
                return Yoga.YGAlignStretch;
            case BASELINE:
                return Yoga.YGAlignBaseline;
            case SPACE_BETWEEN:
                return Yoga.YGAlignSpaceBetween;
            case SPACE_AROUND:
                return Yoga.YGAlignSpaceAround;
            default:
                throw new IllegalArgumentException("Unknown align: " + align);
        }
    }

    private static int justifyValue(Justify justify) {
        switch (justify) {
            case FLEX_START:
                return Yoga.YGJustifyFlexStart;
            case CENTER:
                return Yoga.YGJustifyCenter;
            case FLEX_END:
                return Yoga.YGJustifyFlexEnd;
            case SPACE_BETWEEN:
                return Yoga.YGJustifySpaceBetween;
            case SPACE_AROUND:
                return Yoga.YGJustifySpaceAround;
            case SPACE_EVENLY:
                return Yoga.YGJustifySpaceEvenly;
            default:
                throw new IllegalArgumentException("Unknown justify: " + justify);
        }
    }

    private static int flexDirectionValue(FlexDirection flexDirection) {
        switch (flexDirection) {
            case ROW:
                return Yoga.YGFlexDirectionRow;
            case ROW_REVERSE:
                return Yoga.YGFlexDirectionRowReverse;
            case COLUMN:
                return Yoga.YGFlexDirectionColumn;
            case COLUMN_REVERSE:
                return Yoga.YGFlexDirectionColumnReverse;
            default:
                throw new IllegalArgumentException("Unknown flex direction: " + flexDirection);
        }
    }

    private static int wrapValue(Wrap wrap) {
        switch (wrap) {
            case NO_WRAP:
                return Yoga.YGWrapNoWrap;
            case WRAP:
                return Yoga.YGWrapWrap;
            case WRAP_REVERSE:
                return Yoga.YGWrapReverse;
            default:
                throw new IllegalArgumentException("Unknown wrap: " + wrap);
        }
    }

    private static int positionTypeValue(PositionType positionType) {
        switch (positionType) {
            case RELATIVE:
                return Yoga.YGPositionTypeRelative;
            case ABSOLUTE:
                return Yoga.YGPositionTypeAbsolute;
            default:
                throw new IllegalArgumentException("Unknown position type: " + positionType);
        }
    }

    private static int overflowValue(Overflow overflow) {
        switch (overflow) {
            case VISIBLE:
                return Yoga.YGOverflowVisible;
            case HIDDEN:
                return Yoga.YGOverflowHidden;
            case SCROLL:
                return Yoga.YGOverflowScroll;
            default:
                throw new IllegalArgumentException("Unknown overflow: " + overflow);
        }
    }

    private static int edgeValue(Edge edge) {
        switch (edge) {
            case LEFT:
                return Yoga.YGEdgeLeft;
            case TOP:
                return Yoga.YGEdgeTop;
            case RIGHT:
                return Yoga.YGEdgeRight;
            case BOTTOM:
                return Yoga.YGEdgeBottom;
            case START:
                return Yoga.YGEdgeStart;
            case END:
                return Yoga.YGEdgeEnd;
            case HORIZONTAL:
                return Yoga.YGEdgeHorizontal;
            case VERTICAL:
                return Yoga.YGEdgeVertical;
            case ALL:
                return Yoga.YGEdgeAll;
            default:
                throw new IllegalArgumentException("Unknown edge: " + edge);
        }
    }

    private static YogaMeasureFunction.SizeMode sizeModeFromYoga(int yogaMode) {
        switch (yogaMode) {
            case Yoga.YGMeasureModeExactly:
                return YogaMeasureFunction.SizeMode.EXACTLY;
            case Yoga.YGMeasureModeAtMost:
                return YogaMeasureFunction.SizeMode.AT_MOST;
            case Yoga.YGMeasureModeUndefined:
                return YogaMeasureFunction.SizeMode.UNDEFINED;
            default:
                throw new IllegalArgumentException("Unknown Yoga size mode: " + yogaMode);
        }
    }
}
