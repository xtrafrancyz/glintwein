package net.glintwein.ui;

import net.glintwein.platform.Platform;
import net.glintwein.platform.ScreenType;

public class IngameUILayer extends UILayer {
    @Override
    protected boolean canHandleMouseInput() {
        return super.canHandleMouseInput() && Platform.get().getScreenType() == ScreenType.CHAT;
    }
}
