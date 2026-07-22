package net.glintwein.platform;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;

public class Platform26_1_2 implements Platform {
    public static KeyEvent currentKeyEvent = null;

    private final YogaShim yogaShim = new Yoga3_4_1();
    private final Input input = new Input1_16_5();
    private final Render render = new PlatformRender26_1_2();
    private final Logger logger = new Slf4jLogger();

    @Override
    public long getTimeMillis() {
        return Util.getMillis();
    }

    @Override
    public float getGuiScale() {
        return (float) Minecraft.getInstance().getWindow().getGuiScale();
    }

    @Override
    public int getScreenWidth() {
        return Minecraft.getInstance().getWindow().getScreenWidth();
    }

    @Override
    public int getScreenHeight() {
        return Minecraft.getInstance().getWindow().getScreenHeight();
    }

    @Override
    public int getWindowWidth() {
        return Minecraft.getInstance().getWindow().getWidth();
    }

    @Override
    public int getWindowHeight() {
        return Minecraft.getInstance().getWindow().getHeight();
    }

    @Override
    public ScreenType getScreenType() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) {
            return ScreenType.NONE;
        } else if (screen instanceof ChatScreen) {
            return ScreenType.CHAT;
        } else {
            return ScreenType.OTHER;
        }
    }

    public GlintImage loadImage(InputStream is) throws IOException {
        NativeImage image = NativeImage.read(NativeImage.Format.RGBA, is);
        return new PlatformImage26_1_2(image);
    }

    @Override
    public YogaShim getYogaShim() {
        return yogaShim;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public Render getRender() {
        return render;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private static class Input1_16_5 implements Input {
        @Override
        public float getMouseX() {
            return (float) Minecraft.getInstance().mouseHandler.xpos();
        }

        @Override
        public float getMouseY() {
            return (float) Minecraft.getInstance().mouseHandler.ypos();
        }

        @Override
        public boolean isMouseGrabbed() {
            return Minecraft.getInstance().mouseHandler.isMouseGrabbed();
        }

        @Override
        public boolean hasControlDown() {
            if (currentKeyEvent != null)
                return currentKeyEvent.hasControlDown();
            return Minecraft.getInstance().hasControlDown();
        }

        @Override
        public boolean hasShiftDown() {
            if (currentKeyEvent != null)
                return currentKeyEvent.hasShiftDown();
            return Minecraft.getInstance().hasShiftDown();
        }

        @Override
        public boolean hasAltDown() {
            if (currentKeyEvent != null)
                return currentKeyEvent.hasAltDown();
            return Minecraft.getInstance().hasAltDown();
        }

        @Override
        public void setClipboard(String text) {
            Minecraft.getInstance().keyboardHandler.setClipboard(text);
        }

        @Override
        public String getClipboard() {
            return Minecraft.getInstance().keyboardHandler.getClipboard();
        }
    }
}
