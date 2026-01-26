package net.glintwein.ui.test;

import net.glintwein.ui.Window;
import net.glintwein.ui.WindowManager;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.element.Image;
import net.glintwein.ui.element.Text;
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

        Element list = new Element();
        list.setMousePressHandler((button, x, y) -> true); // To prevent clicks from propagating to the window
        list.setPadding(Edge.ALL, 5);
        list.setBackground(0x55ff0000);
        list.addChild(new Text("Item 1"));
        list.addChild(new SwitchOnClickText());
        Image pout = new Image(Textures.POUT);
        pout.setWidth(100);
        list.addChild(pout);
        root.addChild(list);
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
