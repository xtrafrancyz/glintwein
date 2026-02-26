package net.glintwein.devtest;

import net.glintwein.ui.Window;
import net.glintwein.ui.element.component.ColorPicker;

public class ColorPickerWindow extends Window {
    public ColorPickerWindow() {
        super("color_picker_window");
        root.addChild(new ColorPicker("Pick a Color", 0xFFFF0000));
    }
}
