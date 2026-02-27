package net.glintwein.devtest;

import net.glintwein.ui.Window;
import net.glintwein.ui.data.FlexDirection;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.element.component.ColorPicker;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawRectBuilder;

public class ColorPickerWindow extends Window {
    public ColorPickerWindow() {
        super("color_picker_window");
        root.setFlexDirection(FlexDirection.ROW);

        ColorPicker picker = new ColorPicker("Pick a Color", 0xFFFF0000);
        root.addChild(picker);
        root.addChild(new Element() {
            {
                setSize(1000, 1000);
            }

            @Override
            protected void drawContent(Context ctx) {
                ctx.pose().pushMatrix();
                ctx.pose().translate(contentBox.x, contentBox.y);
                ctx.drawRect(0, 0, 1000, 1000, 0xff000000);

                for (int x = 0; x < 10; x++) {
                    for (int y = 0; y < 10; y++) {
                        ctx.drawRect(DrawRectBuilder.fromXYWH(x * 100, y * 100, 90, 90)
                            .color(picker.getSelectedColor())
                            .outline(0xffffffff, x * 1.3f)
                            .radius(y * 5f)
                        );
                    }
                }
                ctx.pose().popMatrix();
            }
        });
    }
}