package net.glintwein.ui;

import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.test.TestWindow;

import java.util.ArrayList;
import java.util.List;

public class WindowManager {
    public List<Window> windows = new ArrayList<>();

    public WindowManager() {
        addWindow(new TestWindow());
    }

    public void addWindow(Window window) {
        windows.add(window);
    }

    public void render() {
        Context ctx = new Context();
        for (Window w : new ArrayList<>(windows)) {
            w.tick();
        }
        for (Window window : windows) {
            window.draw(ctx);
        }
        ctx.execute();
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

    public boolean onMouseScroll(float mouseX, float mouseY, float amount, float vertical) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (window.onMouseScroll(mouseX, mouseY, amount, vertical)) {
                return true;
            }
        }
        return false;
    }
}
