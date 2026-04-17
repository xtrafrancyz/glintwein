package net.glintwein.platform;

import net.glintwein.ui.data.Size;

public interface YogaMeasureFunction {
    Size measure(float width, SizeMode widthMode, float height, SizeMode heightMode);

    enum SizeMode {
        EXACTLY,
        AT_MOST,
        UNDEFINED;
    }
}
