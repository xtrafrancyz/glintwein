package net.glintwein.demo;

import net.glintwein.Glintwein;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.Window;
import net.glintwein.ui.data.*;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.element.Text;
import net.glintwein.ui.element.TextInput;
import net.glintwein.ui.element.VerticalScrollView;
import net.glintwein.ui.element.component.Dropdown;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawRectBuilder;
import net.glintwein.ui.render.command.DrawTextBuilder;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.util.ARGB;
import net.glintwein.ui.util.Animated;
import net.glintwein.ui.util.Easing;

public class DemoWindow extends Window {
    public static final String ANIME_URL = "https://other.xtrafrancyz.net/graphen/chise.png";

    private static final int BG = 0xff17181c;
    private static final int BG2 = 0xff23262f;
    private static final int BG_ACCENT = 0xffb3c7ff;
    private static final int TEXT_COLOR = 0xffc1c3c8;
    private static final int TEXT_INVERT = 0xff17264f;
    private static final int WHITE = 0xffffffff;
    private static final int BLUE = 0xff82AAFF;
    private static final int GREEN = 0xffC5E478;
    private static final int ORANGE = 0xffF78C6C;

    public DemoWindow() {
        super("glintwein_demo");
        init();
    }

    private void init() {
        this.root.clearChildren();
        root.setMinWidth(300);
        root.setBackground(BG);
        root.setBorderRadius(BorderRadius.of(5));
        root.addChild(titleBar());
        root.addChild(new Collapse("Drawing", new DrawingDemo()));
        root.addChild(new Collapse("Text Input", new InputDemo()));
        root.addChild(new Collapse("Popup", new PopupDemo()));
    }

    private Element titleBar() {
        Element titleBar = new Element();
        titleBar.setFlexDirection(FlexDirection.ROW);
        titleBar.setBackground(BG2);
        titleBar.setJustifyContent(Justify.SPACE_BETWEEN);
        titleBar.setPadding(Edge.ALL, 5);
        titleBar.setBorderRadius(new BorderRadius().top(5));
        titleBar.setMargin(Edge.BOTTOM, 5);

        Text refreshButton = new Text("refresh");
        refreshButton.setAlignSelf(Align.CENTER);
        refreshButton.setFont(refreshButton.getFont().withSize(12));
        refreshButton.setOnClick(button -> {
            init();
            return true;
        });
        refreshButton.setOnMouseEnter(() -> {
            refreshButton.setBackground(BG_ACCENT);
        });
        refreshButton.setOnMouseExit(() -> {
            refreshButton.setBackground(0);
        });

        titleBar.addChild(new Text("Glintwein Demo"));
        titleBar.addChild(refreshButton);

        return titleBar;
    }

    private static class Collapse extends Element {
        boolean expanded = false;

        Collapse(String title, Element content) {
            setMargin(Edge.ALL, 5);
            setMargin(Edge.TOP, 0);

            VerticalScrollView scrollView = new VerticalScrollView();
            Animated.Float height = new Animated.Float(scrollView::setMaxHeight, 0, 300, Easing.EASE);
            trackAnimation(height);

            int titleColor = ARGB.lerp(0.2f, BG, BG_ACCENT);
            int titleHoverColor = ARGB.lerp(0.3f, BG, BG_ACCENT);

            Text titleText = new Text(title);
            Animated.Color titleColorAnim = new Animated.Color(titleText::setBackground, titleColor, 200, Easing.EASE);
            titleText.trackAnimation(titleColorAnim);
            titleText.setPadding(Edge.ALL, 4);
            titleText.setOnMouseEnter(() -> {
                titleColorAnim.animate(titleHoverColor);
            });
            titleText.setOnMouseExit(() -> {
                titleColorAnim.animate(titleColor);
            });
            titleText.setOnClick(button -> {
                expanded = !expanded;
                height.animate(expanded ? 300 : 0);
                scrollView.setDisplay(Display.FLEX);
                if (!expanded)
                    height.createFuture().thenAccept(end -> scrollView.setDisplay(Display.NONE));
                return true;
            });

            scrollView.setBackground(BG2);
            scrollView.addChild(content);

            this.addChild(titleText);
            this.addChild(scrollView);
        }
    }

    private static class DrawingDemo extends Element {
        public DrawingDemo() {
            this.setHeight(250);
            this.setPadding(Edge.ALL, 5);
        }

        @Override
        protected void drawContent(Context ctx) {
            ctx.pose().pushMatrix();
            ctx.pose().translate(contentBox.x, contentBox.y);

            float y = 0;
            ctx.drawRect(DrawRectBuilder.fromXYWH(0, y, 20, 20)
                .color(ORANGE)
            );
            ctx.drawRect(DrawRectBuilder.fromXYWH(30, y, 20, 20)
                .color(ORANGE)
                .radius(5)
            );
            ctx.drawRect(DrawRectBuilder.fromXYWH(60, y, 20, 20)
                .color(ORANGE)
                .radius(20)
            );
            ctx.drawRect(DrawRectBuilder.fromXYWH(90, y, 20, 20)
                .outline(WHITE, 3)
            );
            ctx.drawRect(DrawRectBuilder.fromXYWH(120, y, 20, 20)
                .radius(5)
                .outline(WHITE, 3)
            );
            ctx.drawRect(DrawRectBuilder.fromXYWH(150, y, 20, 20)
                .radius(20)
                .outline(WHITE, 3)
            );

            y += 30;
            ctx.drawRect(DrawRectBuilder.fromXYWH(0, y, 20, 20)
                .color(ORANGE)
                .outline(WHITE, 3)
            );
            ctx.drawRect(DrawRectBuilder.fromXYWH(30, y, 20, 20)
                .color(ORANGE)
                .radius(5)
                .outline(WHITE, 3)
            );
            ctx.drawRect(DrawRectBuilder.fromXYWH(60, y, 20, 20)
                .color(ORANGE)
                .radius(20)
                .outline(WHITE, 3)
            );
            ctx.drawRect(DrawRectBuilder.fromXYWH(90, y, 20, 20)
                .color(Gradient.leftToRight(BLUE, GREEN))
                .outline(WHITE, 3)
            );
            ctx.drawRect(DrawRectBuilder.fromXYWH(120, y, 20, 20)
                .color(Gradient.topToBottom(BLUE, GREEN))
                .radius(5)
                .outline(WHITE, 3)
            );
            ctx.drawRect(DrawRectBuilder.fromXYWH(150, y, 20, 20)
                .color(Gradient.fromCorners(BLUE, GREEN, ORANGE, BLUE))
            );

            y += 30;

            ctx.drawLine(0, y, 20, y + 20, 3, BorderRadius.ZERO, WHITE);
            ctx.drawLine(30, y, 50, y + 20, 4, BorderRadius.of(5), WHITE);

            y += 30;

            GigaFont font = GlobalUIState.getDefaultFont();
            ctx.drawText(font, "Regular", 0, y, 16, TEXT_COLOR);
            ctx.drawText(font, "Small", 0, y + 20, 8, TEXT_COLOR);
            ctx.drawText(font, "Huge", 70, y - 10, 64, TEXT_COLOR);

            y += 60;

            ctx.drawText(DrawTextBuilder.of("Gradient", font, 32)
                .color(Gradient.topToBottom(BLUE, ORANGE))
                .offset(0, y)
            );
            ctx.drawText(DrawTextBuilder.of("Outline", font, 32)
                .color(ORANGE)
                .outline(WHITE, 0.1f)
                .offset(130, y)
            );

            y += 44;

            // cool animation
            if (ctx.pushScissor(Bounds.fromMinMax(0, y, contentBox.width, y + 20))) {
                int[] hueColors = new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
                float period = 3000f;
                float offset = (Glintwein.time % period) / period * contentBox.width;
                float half_pixel = GlobalUIState.getPixelSize() * 0.5f;
                float w = contentBox.width / 6f;
                for (int i = 0; i < 14; i++) {
                    float x = w * i - offset;
                    ctx.drawRect(x, y, w + half_pixel, 20, BorderRadius.ZERO,
                        Gradient.leftToRight(hueColors[i % 6], hueColors[(i + 1) % 6]));
                }
                ctx.popScissor();
            }

            ctx.pose().popMatrix();
        }
    }

    public static class InputDemo extends Element {
        public InputDemo() {
            this.setPadding(Edge.ALL, 5);

            int color = ARGB.lerp(0.2f, BG, BG_ACCENT);

            TextInput input = new TextInput();
            input.setBackground(color);
            input.setPlaceholder("Max length 20");
            input.setMaxLength(20);
            input.setPadding(Edge.ALL, 5);
            this.addChild(input);

            TextInput maxWidthInput = new TextInput();
            maxWidthInput.setBackground(color);
            maxWidthInput.setPlaceholder("Fixed width");
            maxWidthInput.setWidth(150);
            maxWidthInput.setPadding(Edge.ALL, 5);
            maxWidthInput.setMargin(Edge.TOP, 5);
            this.addChild(maxWidthInput);

            TextInput regexInput = new TextInput();
            regexInput.setBackground(color);
            regexInput.setPlaceholder("Custom select color");
            regexInput.setHighlightColor(0x88ff0000);
            regexInput.setPadding(Edge.ALL, 5);
            regexInput.setMargin(Edge.TOP, 5);
            this.addChild(regexInput);

            TextInput multilineInput = new TextInput();
            multilineInput.setBackground(color);
            multilineInput.setPlaceholder("Multiline...");
            multilineInput.setMultiline(true);
            multilineInput.setPadding(Edge.ALL, 5);
            multilineInput.setMargin(Edge.TOP, 5);
            multilineInput.setMaxWidth(300 - 20); // 20 is padding
            this.addChild(multilineInput);
        }
    }

    public static class PopupDemo extends Element {
        public PopupDemo() {
            this.setPadding(Edge.ALL, 5);

            Text text = new Text("Open Popup");
            text.setAlignSelf(Align.FLEX_START);
            text.setOnMouseEnter(() -> text.setBackground(BG_ACCENT));
            text.setOnMouseExit(() -> text.setBackground(0));
            text.setOnClick(button -> {
                Dropdown dd = Dropdown.openCenteredBelowRelative(text, getRoot());
                dd.setWidth(200);
                dd.setBackground(0x88000000);
                dd.setBorderRadius(BorderRadius.of(5));
                dd.setPadding(Edge.ALL, 5);
                dd.addChild(new Text("Hello from popup!"));
                return true;
            });
            this.addChild(text);

            Element hoverContainer = new Element();
            hoverContainer.setMargin(Edge.TOP, 5);
            this.addChild(hoverContainer);

            Element hoverBox = new Element() {
                @Override
                public void draw(Context ctx) {
                    ctx.pushDrawPriority(1);
                    super.draw(ctx);
                    ctx.popDrawPriority(1);
                }
            };
            hoverBox.setBackground(0x88000000);
            hoverBox.setBorderRadius(BorderRadius.of(5));
            hoverBox.setPadding(Edge.ALL, 5);
            hoverBox.setDisplay(Display.NONE);
            hoverBox.setPositionType(PositionType.ABSOLUTE);
            hoverBox.setPosition(Edge.BOTTOM, 0);
            hoverBox.setWidth(150);
            hoverBox.addChild(new Text("You hovered over the text!"));
            hoverContainer.addChild(hoverBox);

            Text text2 = new Text("Hover over me");
            text2.setAlignSelf(Align.FLEX_START);
            text2.setMargin(Edge.TOP, 5);
            this.addChild(text2);

            // animating opacity of hoverBox when hovering over text2
            Animated.Float hoverOpacity = new Animated.Float(hoverBox::setOpacity, 0, 300, Easing.EASE);
            trackAnimation(hoverOpacity);
            text2.setOnMouseEnter(() -> {
                hoverOpacity.animate(1);
                hoverBox.setDisplay(Display.FLEX);
            });
            text2.setOnMouseExit(() -> {
                hoverOpacity.animate(0);
                hoverOpacity.createFuture().thenAccept(end -> hoverBox.setDisplay(Display.NONE));
            });
        }
    }
}
