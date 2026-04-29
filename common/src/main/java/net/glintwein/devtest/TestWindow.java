package net.glintwein.devtest;

import net.glintwein.Glintwein;
import net.glintwein.platform.Platform;
import net.glintwein.ui.Window;
import net.glintwein.ui.WindowManager;
import net.glintwein.ui.data.*;
import net.glintwein.ui.element.*;
import net.glintwein.ui.element.component.Dropdown;
import net.glintwein.ui.element.component.Slider;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawRectBuilder;
import net.glintwein.ui.render.command.DrawTextBuilder;
import net.glintwein.ui.render.texture.Textures;
import net.glintwein.ui.util.Animated;
import net.glintwein.ui.util.Easing;

import java.util.Arrays;
import java.util.List;

public class TestWindow extends Window {
    public TestWindow() {
        super("test_window");
        root.setPadding(Edge.ALL, 5);
        root.setBackground(0x88ffffff);

        root.addChild(new Text("Test Window"));

        VerticalScrollView list = new VerticalScrollView();
        list.setMaxWidth(200);
        list.setHeight(70);
        list.setOnMousePress((button, x, y) -> true); // To prevent clicks from propagating to the window
        list.setPadding(Edge.ALL, 5);
        list.setPadding(Edge.RIGHT, 10);
        list.setBarWidth(6);
        list.setBarPadding(2);
        list.setBackground(0x55ff0000);
        list.addChild(new SwitchOnClickText());
        list.addChild(new Text("Item 1"));
        Image pout = new Image(Textures.POUT);
        pout.setWidth(100);
        list.addChild(pout);
        list.addChild(new SwitchOnClickText());
        root.addChild(list);

        TextInput input = new TextInput();
        input.setMaxWidthPercent(100);
        input.setBackground(0x5500ff00);
        input.setPadding(Edge.ALL, 5);
        root.addChild(input);

        Element colorPickerRow = new Element();
        colorPickerRow.setPadding(Edge.ALL, 5);
        colorPickerRow.setAlignSelf(Align.FLEX_START);
        colorPickerRow.setFlexDirection(FlexDirection.ROW);
        colorPickerRow.addChild(new OpenColorPickerButton());
        colorPickerRow.addChild(new InfoHover("Click the button to open/close the color picker window"));
        colorPickerRow.addChild(new DropdownTest());
        root.addChild(colorPickerRow);

        root.addChild(new SpawnWaypointButton());

        Slider slider = new Slider(200, 400, 200);
        slider.setOnValueChanged(root::setSize);
        root.addChild(slider);
    }

    private static class SpawnWaypointButton extends Button {
        public SpawnWaypointButton() {
            super("Spawn Waypoint Here");

            this.setOnClick((button) -> {
                WaypointElement waypointElement = new WaypointElement() {
                    private final long eol = Glintwein.time + 100000;

                    @Override
                    public void tick() {
                        super.tick();
                        if (Glintwein.time >= eol)
                            this.getParent().removeChild(this);
                        if (isNearScreenCenter(150)) {
                            setWidth(100);
                        } else {
                            setWidth(30);
                        }
                    }
                };
                waypointElement.enableLayoutLerp(400, Easing.OUT_BACK);
                waypointElement.setTargetPos(Platform.get().getRender().getCameraPos().sub(0, 1.8f, 0));
                waypointElement.setSize(30);
                waypointElement.setBackground(0xffff0000);
                Glintwein.instance.layerIngame.getContent().addChild(waypointElement);
                return true;
            });
        }
    }

    private static class OpenColorPickerButton extends Button {
        private ColorPickerWindow colorPicker;

        public OpenColorPickerButton() {
            super("Color Picker");

            WindowManager manager = Glintwein.instance.layerIngame.getWindowManager();
            this.setOnClick((button) -> {
                if (colorPicker == null) {
                    manager.addWindow(colorPicker = new ColorPickerWindow());
                } else {
                    manager.removeWindow(colorPicker);
                    colorPicker = null;
                }
                return true;
            });
        }
    }

    private static class InfoHover extends Element {
        private final Element infoContainer;

        public InfoHover(String info) {
            this.setPadding(Edge.ALL, 5);
            this.setBackground(0x550000ff);
            this.setBorderRadius(BorderRadius.of(5));
            this.setAlignSelf(Align.CENTER);
            this.setMargin(Edge.LEFT, 5);
            this.setSize(20);

            infoContainer = new Element();
            Text infoText = new Text(info);
            infoContainer.addChild(infoText);
            infoContainer.setPositionType(PositionType.ABSOLUTE);
            infoContainer.setBackground(0x550000ff);
            infoContainer.setWidth(200);
            infoContainer.setPosition(Edge.BOTTOM, 10);
            infoContainer.setPosition(Edge.LEFT, 10);
            infoContainer.setPadding(Edge.ALL, 5);
            infoContainer.setDisplay(Display.NONE);
            this.addChild(infoContainer);

            setOnMouseEnter(() -> {
                infoContainer.setDisplay(Display.FLEX);
            });

            setOnMouseExit(() -> {
                infoContainer.setDisplay(Display.NONE);
            });
        }
    }

    private static class DropdownTest extends Element {
        private Dropdown dropdown;

        public DropdownTest() {
            this.setPadding(Edge.ALL, 5);
            this.setBackground(0x5500ff00);
            this.setBorderRadius(BorderRadius.of(5));
            this.setAlignSelf(Align.CENTER);
            this.setMargin(Edge.LEFT, 5);
            this.setSize(20);

            this.setOnClick(button -> {
                if (dropdown != null) {
                    dropdown.close();
                    dropdown = null;
                    return true;
                }
                this.dropdown = Dropdown.openBelow(this);
                this.dropdown.setBackground(0x550000ff);
                this.dropdown.setPadding(Edge.ALL, 5);
                this.dropdown.addChild(new Text("Dropdown Item 1") {
                    @Override
                    public void tick() {
                        super.tick();
                        if (isHovered()) {
                            setBackground(0x33ffffff);
                        } else {
                            setBackground(0x00ffffff);
                        }
                    }
                });
                this.dropdown.addChild(new Text("Dropdown Item 2"));
                return true;
            });
        }
    }

    private static class Button extends Element {
        private static final int BG = 0xff15101E;
        private static final int RIGHT_COLOR = 0xff7C66CD;
        private static final int LEFT_COLOR = 0xff26166C;

        private final Animated.Color rightColor;
        private final Animated.Color leftColor;
        private final Animated.Float outlineWidth;

        public Button(String text) {
            this.addChild(new Text(text));
            this.setPadding(Edge.ALL, 7);
            this.setAlignSelf(Align.FLEX_START);
            rightColor = new Animated.Color(0x550000ff);
            leftColor = new Animated.Color(0x550000ff);
            outlineWidth = new Animated.Float(1);
        }

        @Override
        public void tick() {
            super.tick();
            if (isHovered()) {
                rightColor.animateIfDifferent(RIGHT_COLOR, 200, Easing.EASE);
                leftColor.animateIfDifferent(LEFT_COLOR, 200, Easing.EASE);
                outlineWidth.animateIfDifferent(0, 200, Easing.EASE);
            } else {
                rightColor.animateIfDifferent(BG, 200, Easing.EASE);
                leftColor.animateIfDifferent(BG, 200, Easing.EASE);
                outlineWidth.animateIfDifferent(2, 200, Easing.EASE);
            }
        }

        @Override
        public void drawContent(Context ctx) {
            ctx.drawRect(DrawRectBuilder.fromBox(paddingBox)
                .color(Gradient.leftToRight(leftColor.get(), rightColor.get()))
                .outline(0xff261D3B, outlineWidth.get())
                .radius(8)
            );
        }
    }

    private static class SwitchOnClickText extends Text {
        List<String> loremList = Arrays.asList(
            "1. Lorem ipsum dolor sit amet, consectetur adipiscing elit",
            "2. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua",
            "3. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris",
            "4. Nisi ut aliquip ex ea commodo consequat",
            "11. Хуй пизда и баба ебется",
            "12. Еще строка для теста ширины текста",
            "13. Последняя строка в этом списке",
            "14. The quick brown fox jumps over the lazy dog",
            "15. Pack my box with five dozen liquor jugs",
            "16. How vexingly quick daft zebras jump"
        );

        private final Animated.Color bgAnim;

        public SwitchOnClickText() {
            super("Click me to change text");
            this.setOnClick(button -> {
                int index = (int) (Math.random() * loremList.size());
                this.setText(loremList.get(index));
                return true;
            });
            trackAnimation(bgAnim = new Animated.Color(this::setBackground, 0x00ffffff));
        }

        @Override
        public void tick() {
            super.tick();
            if (isHovered()) {
                bgAnim.animateIfDifferent(0x33ffffff, 200, Easing.EASE);
            } else {
                bgAnim.animateIfDifferent(0x00ffffff, 200, Easing.EASE);
            }
        }

        @Override
        protected void drawLine(Context ctx, RenderLine line) {
            ctx.drawText(DrawTextBuilder.of(line, font)
                .color(Gradient.topToBottom(0xff7C66CD, 0xff26166C))
            );
        }
    }
}
