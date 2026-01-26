package net.glintwein.ui.test;

import com.mojang.blaze3d.platform.Window;
import net.glintwein.ui.element.RootElement;
import net.glintwein.ui.render.command.Context;
import net.minecraft.client.Minecraft;

public class TestUI {
    private final RootElement root;

    public TestUI() {
        root = new RootElement();

        root.addChild(new TestRow());
    }

    public void render() {
        Window window = Minecraft.getInstance().getWindow();
        root.tick();
        root.calculateLayout(window.getGuiScaledWidth(), window.getGuiScaledHeight());

        Context ctx = new Context();
        root.draw(ctx);
        ctx.execute();
    }
}
