package net.glintwein.ui.test;

import net.glintwein.Glintwein;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.FlexDirection;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.element.Text;
import net.glintwein.ui.render.font.Fonts;
import net.glintwein.ui.util.Animated;

import java.util.Arrays;
import java.util.List;

public class TestRow extends Element {
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

    private Element middle;
    private Animated.Float middleWidth;

    public TestRow() {
        setFlexDirection(FlexDirection.ROW);
        //enableLayoutLerp(200, Easing.EASE);
        setPadding(Edge.ALL, 20);
        setBackground(0x77ffffff);
        setFlexShrink(1);
        setPositionType(PositionType.ABSOLUTE);

        Element left = new Element();
        left.setBackground(0xff0000ff);
        left.setSize(50, 30);
        addChild(left);

        middle = new Element();
        middle.setBackground(0xff00ff00);
        middle.setHeight(20);
        middleWidth = new Animated.Float(middle::setWidth, 0);
        middle.trackAnimation(middleWidth);
        addChild(middle);

        Text right = new Text("Demo text");
        right.setBackground(0xffff0000);
        //right.setSize(50, 20);
        addChild(right);
    }

    @Override
    public void tick() {
        super.tick();
        if (Glintwein.time % 2000 < 1000) {
            //middleWidth.set(10);
        } else {
            //if (middleWidth.get() == 10)
            //    middleWidth.animate(100, 200, Easing.EASE);
        }
        int line = (int) (Glintwein.time / 1000 % loremList.size());
        ((Text) getChildren().get(2)).setText(loremList.get(line));
        ((Text) getChildren().get(2)).setFont(Fonts.REGULAR, 20);
    }
}
