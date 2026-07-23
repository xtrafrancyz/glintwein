package net.glintwein.ui.element.component;

import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.Align;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawRectBuilder;
import net.glintwein.ui.render.font.SizedFont;
import net.glintwein.ui.rtf.RichContent;
import net.glintwein.ui.util.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CurrencyPlot extends Element {
    private SizedFont font;
    private float[] data = new float[1];
    private float dataMin;
    private float dataMax;
    private float lineWidth = 1;
    private int upperColor = 0xff16C784;
    private int lowerColor = 0xffEA3943;
    private int rulerColor = 0xff888888;
    private int hoverLabelColor = 0xff1E1E1E;
    private int yAxisLavelTextColor = 0xffffffff;
    private int yAxisLevelColor = 0x88888888;
    private float yAxisLabelPaddingLeft = 2;
    private float yAxisLabelPaddingRight = 3;

    private PlotCtx pctx = new PlotCtx();
    private int hoveredIndex = -1;
    private float mouseX;
    private float mouseY;
    private TooltipFactory tooltipFactory;
    private final Element tooltipContainer;
    private boolean tooltipDirty = false;

    public CurrencyPlot() {
        font = GlobalUIState.getDefaultTextFont();

        tooltipContainer = new Element();
        tooltipContainer.enableLayoutLerp(250, Easing.IN_SINE);
        tooltipContainer.setPositionType(PositionType.ABSOLUTE);
        tooltipContainer.setAlignItems(Align.FLEX_START);
        addChild(tooltipContainer);

        tooltipFactory = (index, value) -> {
            RichElement el = new RichElement(RichContent.builder()
                .append("Index: ").color(0xffC5E478).append("" + index)
                .append("\n")
                .color(0xffffffff)
                .append("Value: ").color(0xffC5E478).append(String.format("%.2f", value))
                .build()) {
                @Override
                public void draw(Context ctx) {
                    ctx.pushDrawPriority(10);
                    super.draw(ctx);
                    ctx.popDrawPriority(10);
                }
            };
            el.setPadding(Edge.ALL, 5);
            el.setBackground(0xff1E1E1E);
            el.setBorderRadius(new BorderRadius(5));
            return el;
        };
    }

    public void setData(float[] data) {
        this.data = data;
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (float value : data) {
            if (value < min)
                min = value;
            if (value > max)
                max = value;
        }
        dataMin = min;
        dataMax = max;
        tooltipDirty = true;
        pctx.yLabelsDirty = true;
    }

    public void setFont(SizedFont font) {
        this.font = font;
    }

    public void setTooltip(TooltipFactory factory) {
        this.tooltipFactory = factory;
    }

    public void setLineWidth(float width) {
        this.lineWidth = width;
    }

    public void setLineColor(int upper, int lower) {
        this.upperColor = upper;
        this.lowerColor = lower;
    }

    public void setRulerColor(int color) {
        this.rulerColor = color;
    }

    public void setYAxisLabelHoverColor(int color) {
        this.hoverLabelColor = color;
    }

    public void setYAxisLabelTextColor(int color) {
        this.yAxisLavelTextColor = color;
    }

    public void setYAxisLabelPadding(float left, float right) {
        this.yAxisLabelPaddingLeft = left;
        this.yAxisLabelPaddingRight = right;
    }

    public void setYAxisLevelColor(int color) {
        this.yAxisLevelColor = color;
    }

    @Override
    protected void handleMouseMoved(float mouseX, float mouseY, boolean canHover) {
        super.handleMouseMoved(mouseX, mouseY, canHover);
        if (data.length == 0)
            return;

        if (isHovered()) {
            this.mouseX = mouseX = mouseX - pctx.x;
            this.mouseY = mouseY = mouseY - pctx.y;
            float segmentWidth = pctx.width / (data.length - 1);
            int index = GMath.clamp(Math.round(mouseX / segmentWidth), 0, data.length - 1);
            float value = data[index];

            if (tooltipDirty || tooltipContainer.getChildren().isEmpty() || hoveredIndex != index) {
                hoveredIndex = index;
                if (tooltipFactory != null) {
                    tooltipContainer.clearChildren();
                    tooltipContainer.addChild(tooltipFactory.create(index, value));
                }
            }
        } else {
            hoveredIndex = -1;
            tooltipContainer.clearChildren();
        }
        tooltipDirty = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!tooltipContainer.getChildren().isEmpty()) {
            tooltipContainer.setPosition(Edge.TOP, mouseY - tooltipContainer.getComputedHeight() / 2);
            tooltipContainer.setPosition(Edge.LEFT, mouseX - tooltipContainer.getComputedWidth() - 10);
        }
        pctx = initPlotCtx();
    }

    private PlotCtx initPlotCtx() {
        if (pctx.yLabelsDirty || pctx.contentHeightCache != contentBox.height) {
            pctx.contentHeightCache = contentBox.height;
            pctx.yLabelsDirty = false;

            int maxTicks = Math.max(2, (int) Math.floor(contentBox.height / (font.getHeight() * 1.5f)));
            pctx.yAxisLabels = new ArrayList<>();
            double tickSpacing = NiceNumbers.niceNum(NiceNumbers.niceNum(dataMax - dataMin, false) / (maxTicks - 1), true);
            double[] ticks = NiceNumbers.niceTicks(dataMin, dataMax, maxTicks);
            pctx.yAxisFormatter = NiceNumbers.Formatter.forSpacing(tickSpacing * 0.1); // 0.1 to add one extra zero

            float maxLabelWidth = 0;
            for (double tick : ticks) {
                Label label = new Label((float) tick, NiceNumbers.Formatter.format(pctx.yAxisFormatter, tick, tickSpacing));
                pctx.yAxisLabels.add(label);
                float labelWidth = font.getWidth(label.formattedText);
                if (labelWidth > maxLabelWidth)
                    maxLabelWidth = labelWidth;
            }
            pctx.yAxisWidth = maxLabelWidth + yAxisLabelPaddingLeft + yAxisLabelPaddingRight;
        }

        pctx.x = contentBox.x;
        pctx.y = contentBox.y;
        pctx.width = contentBox.width - pctx.yAxisWidth;
        pctx.height = contentBox.height;
        pctx.yScale = pctx.height / (dataMax - dataMin);
        pctx.separator = pctx.dataY(0);
        return pctx;
    }

    @Override
    protected void drawContent(Context ctx) {
        if (data.length == 0)
            return;
        ctx.pose().pushMatrix();
        ctx.pose().translate(ctx.roundPixelX(contentBox.x), ctx.roundPixelY(contentBox.y));

        drawYAxisLevels(ctx, pctx);
        plotLine(ctx, pctx);
        drawDottedLine(ctx, 0, pctx.separator, pctx.width, pctx.separator, rulerColor);
        drawYAxis(ctx, pctx);
        drawAxisLabelBackground(ctx, pctx, data[data.length - 1], pctx.valueColor(data[data.length - 1]));
        drawHoverData(ctx, pctx);

        ctx.pose().popMatrix();
    }

    private void drawHoverData(Context ctx, PlotCtx pctx) {
        if (hoveredIndex == -1 || hoveredIndex >= data.length)
            return;
        float height = pctx.height;

        // lines
        float segmentWidth = pctx.width / (data.length - 1);
        float x = ctx.roundPixelX(hoveredIndex * segmentWidth);
        float y = mouseY;
        int color = ARGB.mulAlpha(rulerColor, 0.6f);
        drawDottedLine(ctx, x, 0, x, height, color);
        drawDottedLine(ctx, 0, y, pctx.width, y, color);

        // point
        y = ctx.roundPixelY(pctx.dataY(hoveredIndex));
        float rad = lineWidth * 3;
        ctx.drawRect(DrawRectBuilder.fromXYWH(x - rad, y - rad, rad * 2, rad * 2)
            .radius(new BorderRadius(rad))
            .color(y <= pctx.separator ? upperColor : lowerColor)
            .outline(0xffffffff, Math.max(0.5f, ctx.getPixelSize()))
        );

        // y-axis label
        float range = dataMax - dataMin;
        float mo = dataMin + (range * (height - mouseY) / height);
        drawAxisLabelBackground(ctx, pctx, mo, hoverLabelColor);
    }

    private void drawAxisLabelBackground(Context ctx, PlotCtx pctx, float value, int color) {
        float y = pctx.valueY(value) - font.getHeight() * 0.5f;
        ctx.drawRect(DrawRectBuilder.fromXYWH(pctx.width, y, contentBox.width - pctx.width, font.getHeight())
            .color(color)
            .radius(new BorderRadius(3))
        );
        ctx.drawText(font.font(), pctx.yAxisFormatter.format(value), pctx.width + yAxisLabelPaddingLeft, y, font.size(), yAxisLavelTextColor);
    }

    private void plotLine(Context ctx, PlotCtx pctx) {
        BorderRadius radius = new BorderRadius(lineWidth);
        float segmentWidth = pctx.width / (data.length - 1);
        float separator = pctx.separator;

        for (int i = 0; i < data.length - 1; i++) {
            float xStart = ctx.roundPixelX(i * segmentWidth);
            float xEnd = ctx.roundPixelX((i + 1) * segmentWidth);
            float yStart = pctx.dataY(i);
            float yEnd = pctx.dataY(i + 1);
            int startColor = yStart <= separator ? upperColor : lowerColor;
            int endColor = yEnd <= separator ? upperColor : lowerColor;
            if (startColor != endColor) {
                // draw line in two segments with different colors
                float midX = xStart + (xEnd - xStart) * (separator - yStart) / (yEnd - yStart);
                float midY = separator;
                ctx.drawLine(xStart, yStart, midX, midY, lineWidth, BorderRadius.ZERO, startColor);
                ctx.drawLine(midX, midY, xEnd, yEnd, lineWidth, BorderRadius.ZERO, endColor);
            } else {
                ctx.drawLine(xStart, yStart, xEnd, yEnd, lineWidth, BorderRadius.ZERO, startColor);
            }

            // circle to fill crevices between lines
            if (i != 0)
                ctx.drawRect(xStart - lineWidth * 0.5f, yStart - lineWidth * 0.5f, lineWidth, lineWidth, radius, startColor);
        }
    }

    private void drawDottedLine(Context ctx, float x1, float y1, float x2, float y2, int color) {
        float lineWidth = Math.max(1, ctx.getPixelSize());
        BorderRadius radius = new BorderRadius(lineWidth);
        float segmentLength = 1;
        float gapLength = 2;
        float totalLength = GMath.sqrt(GMath.square(x2 - x1) + GMath.square(y2 - y1));
        int numSegments = (int) (totalLength / (segmentLength + gapLength));
        for (int i = 0; i < numSegments; i++) {
            float startX = x1 + (x2 - x1) * (i * (segmentLength + gapLength)) / totalLength;
            float startY = y1 + (y2 - y1) * (i * (segmentLength + gapLength)) / totalLength;
            float endX = x1 + (x2 - x1) * ((i * (segmentLength + gapLength)) + segmentLength) / totalLength;
            float endY = y1 + (y2 - y1) * ((i * (segmentLength + gapLength)) + segmentLength) / totalLength;
            ctx.drawLine(startX, startY, endX, endY, lineWidth, radius, color);
        }
    }

    private void drawYAxisLevels(Context ctx, PlotCtx pctx) {
        for (Label label : pctx.yAxisLabels) {
            float y = pctx.valueY(label.value);
            ctx.drawLine(0, y, pctx.width, y, ctx.getPixelSize(), BorderRadius.ZERO, yAxisLevelColor);
        }
    }

    private void drawYAxis(Context ctx, PlotCtx pctx) {
        for (Label label : pctx.yAxisLabels) {
            float y = pctx.valueY(label.value) - font.getHeight() * 0.5f;
            ctx.drawText(font.font(), label.formattedText, pctx.width + yAxisLabelPaddingLeft, y, font.size(), yAxisLavelTextColor);
        }
    }

    private class PlotCtx {
        float x;
        float y;
        float width;
        float height;
        float yScale;
        float separator;

        float yAxisWidth;
        List<Label> yAxisLabels;
        DecimalFormat yAxisFormatter;
        boolean yLabelsDirty = true;
        float contentHeightCache = -1;

        float dataY(int index) {
            return height - (data[index] - dataMin) * yScale;
        }

        float valueY(float value) {
            return height - (value - dataMin) * yScale;
        }

        int valueColor(float value) {
            return value <= data[0] ? lowerColor : upperColor;
        }
    }

    private static class Label {
        final float value;
        final String formattedText;

        Label(float value, String formattedText) {
            this.value = value;
            this.formattedText = formattedText;
        }
    }

    public interface TooltipFactory {
        Element create(int index, float value);
    }
}
