package net.glintwein.ui.test;

import net.glintwein.ui.Window;
import net.glintwein.ui.WindowManager;
import net.glintwein.ui.data.Align;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.Gradient;
import net.glintwein.ui.element.*;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawRectBuilder;
import net.glintwein.ui.render.texture.Textures;
import net.glintwein.ui.util.Animated;
import net.glintwein.ui.util.Easing;

import java.util.Arrays;
import java.util.List;

public class TestWindow extends Window {
    public TestWindow(WindowManager manager) {
        super(manager, "test_window");
        root.setPadding(Edge.ALL, 5);
        root.setBackground(0x88ffffff);
        root.setMaxWidth(200);

        root.addChild(new Text("Test Window"));

        VerticalScrollView list = new VerticalScrollView();
        list.setHeight(70);
        list.setMousePressHandler((button, x, y) -> true); // To prevent clicks from propagating to the window
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

        root.addChild(new OpenColorPickerButton(manager));
    }

    private static class OpenColorPickerButton extends Button {
        private ColorPickerWindow colorPicker;

        public OpenColorPickerButton(WindowManager manager) {
            super("Color Picker");

            this.setClickHandler((button) -> {
                if (colorPicker == null) {
                    manager.addWindow(colorPicker = new ColorPickerWindow(manager));
                } else {
                    manager.removeWindow(colorPicker);
                    colorPicker = null;
                }
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
        public void draw(Context ctx) {
            ctx.drawRect(DrawRectBuilder.fromBox(paddingBox)
                .color(Gradient.leftToRight(leftColor.get(), rightColor.get()))
                .outline(0xff261D3B, outlineWidth.get())
                .radius(8)
            );
            super.draw(ctx);
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
            this.setClickHandler(button -> {
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
    }
}
