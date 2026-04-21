package net.glintwein.ui.imui;

import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.font.SFPro;

public class ImguiStyle {
    public GigaFont font = SFPro.REGULAR;
    public float fontSize = 16;
    public int[] colors = new int[IMColor.values().length];
    public float itemSpacingX = 8;
    public float itemSpacingY = 4;
    public float windowPaddingX = 8;
    public float windowPaddingY = 8;

    public ImguiStyle() {
        initDark();
    }

    public void initDark() {
        colors[IMColor.TEXT.index] = 0xFFFFFFFF;
        colors[IMColor.TEXT_DISABLED.index] = 0xFF808080;
        colors[IMColor.WINDOW_BACKGROUND.index] = 0xF00D0D0D;
        colors[IMColor.CHILD_BACKGROUND.index] = 0x00000000;
    }
}
