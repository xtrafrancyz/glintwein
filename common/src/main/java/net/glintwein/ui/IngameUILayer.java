package net.glintwein.ui;

import net.glintwein.platform.Platform;

public class IngameUILayer extends UILayer {
    @Override
    protected boolean canHandleMouseInput() {
        return super.canHandleMouseInput() && Platform.get().getInput().canHandleIngameLayerInput();
    }
}
