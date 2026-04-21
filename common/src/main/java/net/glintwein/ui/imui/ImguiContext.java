package net.glintwein.ui.imui;

import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.imui.state.ImguiWindow;
import net.glintwein.ui.render.command.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImguiContext {
    public ImguiWindow currentWindow;
    public Map<String, ImguiWindow> windows = new HashMap<>();
    public Context draw;
    public ImguiStyle style = new ImguiStyle();
    List<ModColor> colorStack = new ArrayList<>();
    private boolean initialized;

    public int lastItemId;
    public final Bounds lastItemBB = Bounds.fromXYWH(0, 0, 0, 0);

    public int activeId;
    public int activeIdPrevFrame;

    public ImguiContext() {

    }

    public void newFrame(Context renderContext) {
        if (!initialized) {
            initialized = true;
        }
        this.draw = renderContext;
    }

    static class ModColor {
        public int index;
        public int color;

        public ModColor(int index, int color) {
            this.index = index;
            this.color = color;
        }
    }
}
