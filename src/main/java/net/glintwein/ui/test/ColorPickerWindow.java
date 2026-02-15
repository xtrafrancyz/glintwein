package net.glintwein.ui.test;

import net.glintwein.ui.Window;
import net.glintwein.ui.WindowManager;
import net.glintwein.ui.element.component.ColorPicker;

public class ColorPickerWindow extends Window {
    public ColorPickerWindow(WindowManager manager) {
        super(manager, "color_picker_window");
        root.addChild(new ColorPicker("Pick a Color", 0xFFFF0000));
    }
}
