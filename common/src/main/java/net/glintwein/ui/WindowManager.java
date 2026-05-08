package net.glintwein.ui;

import net.glintwein.ui.render.command.Context;

import java.util.ArrayList;
import java.util.List;

public class WindowManager {
    public final UILayer layer;
    public List<Window> windows = new ArrayList<>();

    public WindowManager(UILayer layer) {
        this.layer = layer;
    }

    public void addWindow(Window window) {
        window.windowManager = this;
        windows.add(window);
    }

    public void removeWindow(Window window) {
        if (window.windowManager != this)
            return;
        window.windowManager = null;
        windows.remove(window);
    }

    void invalidateLayout() {
        for (Window w : windows) {
            w.invalidateLayout();
        }
    }

    public void tick(float mouseX, float mouseY, boolean mouseReleased) {
        for (Window w : new ArrayList<>(windows)) {
            w.tick(mouseX, mouseY, mouseReleased);
        }
    }

    public void draw(Context ctx) {
        for (Window window : windows) {
            window.draw(ctx);
        }
    }

    public boolean onMousePress(float mouseX, float mouseY, int button) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (window.onMousePress(mouseX, mouseY, button)) {
                // Move to front
                if (i != windows.size() - 1) {
                    windows.remove(i);
                    windows.add(window);
                }
                return true;
            }
        }
        return false;
    }

    public boolean onMouseRelease(float mouseX, float mouseY, int button) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (window.onMouseRelease(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    public boolean onMouseScroll(float mouseX, float mouseY, float horizontal, float vertical) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (window.onMouseScroll(mouseX, mouseY, horizontal, vertical)) {
                return true;
            }
        }
        return false;
    }
}
