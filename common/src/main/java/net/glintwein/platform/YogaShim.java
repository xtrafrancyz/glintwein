package net.glintwein.platform;

import net.glintwein.ui.data.*;
import org.lwjgl.system.NativeResource;

public interface YogaShim {
    long ConfigNew();

    void ConfigSetPointScaleFactor(long config, float pixelsInPoint);

    long NodeNewWithConfig(long config);

    void NodeFree(long node);

    void NodeStyleSetDisplay(long node, Display display);

    void NodeStyleSetAspectRatio(long node, float aspectRatio);

    void NodeStyleSetAlignContent(long node, Align align);

    void NodeStyleSetAlignItems(long node, Align align);

    void NodeStyleSetAlignSelf(long node, Align align);

    void NodeStyleSetJustifyContent(long node, Justify justify);

    void NodeStyleSetFlex(long node, float flex);

    void NodeStyleSetFlexGrow(long node, float flexGrow);

    void NodeStyleSetFlexShrink(long node, float flexShrink);

    void NodeStyleSetFlexBasis(long node, float flexBasis);

    void NodeStyleSetFlexBasisPercent(long node, float percent);

    void NodeStyleSetFlexBasisAuto(long node);

    void NodeStyleSetFlexDirection(long node, FlexDirection flexDirection);

    void NodeStyleSetFlexWrap(long node, Wrap wrap);

    void NodeStyleSetWidth(long node, float width);

    void NodeStyleSetHeight(long node, float height);

    void NodeStyleSetWidthPercent(long node, float percent);

    void NodeStyleSetHeightPercent(long node, float percent);

    void NodeStyleSetWidthAuto(long node);

    void NodeStyleSetHeightAuto(long node);

    void NodeStyleSetMinWidth(long node, float minWidth);

    void NodeStyleSetMinHeight(long node, float minHeight);

    void NodeStyleSetMinWidthPercent(long node, float percent);

    void NodeStyleSetMinHeightPercent(long node, float percent);

    void NodeStyleSetMaxWidth(long node, float maxWidth);

    void NodeStyleSetMaxHeight(long node, float maxHeight);

    void NodeStyleSetMaxWidthPercent(long node, float percent);

    void NodeStyleSetMaxHeightPercent(long node, float percent);

    void NodeStyleSetPositionType(long node, PositionType positionType);

    void NodeStyleSetPosition(long node, Edge edge, float position);

    void NodeStyleSetPositionPercent(long node, Edge edge, float percent);

    void NodeStyleSetMargin(long node, Edge edge, float margin);

    void NodeStyleSetMarginPercent(long node, Edge edge, float percent);

    void NodeStyleSetMarginAuto(long node, Edge edge);

    void NodeStyleSetPadding(long node, Edge edge, float padding);

    void NodeStyleSetPaddingPercent(long node, Edge edge, float percent);

    void NodeStyleSetBorder(long node, Edge edge, float width);

    void NodeStyleSetOverflow(long node, Overflow overflow);

    void NodeInsertChild(long node, long child, int index);

    void NodeRemoveChild(long node, long child);

    void NodeRemoveAllChildren(long node);

    boolean NodeGetHasNewLayout(long node);

    void NodeSetHasNewLayout(long node, boolean hasNewLayout);

    YogaLayoutResult NodeGetLayout(long node);

    boolean NodeLayoutGetHadOverflow(long node);

    void NodeMarkDirty(long node);

    boolean NodeIsDirty(long node);

    void NodeMarkDirtyAndPropogateToDescendants(long node);

    void NodeCalculateLayout(long node, float availableWidth, float availableHeight);

    NativeResource NodeSetMeasureFunc(long node, YogaMeasureFunction measureFunc);
}
