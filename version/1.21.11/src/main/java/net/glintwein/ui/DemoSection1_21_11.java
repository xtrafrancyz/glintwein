package net.glintwein.ui;

import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.FlexDirection;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.render.command.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DemoSection1_21_11 extends Element {
    public DemoSection1_21_11() {
        setPadding(Edge.ALL, 5);

        Element row = new Element();
        row.setFlexDirection(FlexDirection.ROW);
        row.addChild(new ItemElement(new ItemStack(Items.SAND), 50));
        row.addChild(new ItemElement(new ItemStack(Items.STONE_AXE), 50));
        row.addChild(new PlayerHeadElement(50));
        addChild(row);
    }

    private static class ItemElement extends Element {
        private final ItemStack itemStack;

        public ItemElement(ItemStack is, float size) {
            this.itemStack = is;
            this.setSize(size);
        }

        @Override
        protected void drawContent(Context ctx) {
            ContextExt.drawItem(
                ctx, itemStack,
                contentBox.x, contentBox.y,
                Math.min(contentBox.width, contentBox.height),
                false
            );
        }
    }

    private static class PlayerHeadElement extends Element {
        public PlayerHeadElement(float size) {
            this.setSize(size);
        }

        @Override
        protected void drawContent(Context ctx) {
            ContextExt.drawPlayerHead(
                ctx, Minecraft.getInstance().player,
                contentBox.x, contentBox.y,
                Math.min(contentBox.width, contentBox.height),
                BorderRadius.of(5)
            );
        }
    }
}
