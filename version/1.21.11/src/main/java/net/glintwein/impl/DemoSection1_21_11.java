package net.glintwein.impl;

import net.glintwein.demo.DemoWindow;
import net.glintwein.ui.ContextExt;
import net.glintwein.ui.GlintweinTooltip;
import net.glintwein.ui.data.Align;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.FlexDirection;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.element.Text;
import net.glintwein.ui.element.component.RichElement;
import net.glintwein.ui.render.command.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class DemoSection1_21_11 extends Element {
    private static boolean tooltipFactoryRegistered = false;

    private static void registerTooltipFactory() {
        if (!tooltipFactoryRegistered) {
            GlintweinTooltip.register("glintwein_demo", (data) -> {
                if (data.equals("diamond")) {
                    ItemStack is = new ItemStack(Items.IRON_AXE);
                    List<Component> tooltip = is.getTooltipLines(
                        Item.TooltipContext.of(Minecraft.getInstance().level),
                        Minecraft.getInstance().player,
                        TooltipFlag.ADVANCED
                    );
                    return new RichElement(GlintweinTooltip.mcToRichContent(tooltip));
                }
                return null;
            });
            tooltipFactoryRegistered = true;
        }
    }

    public DemoSection1_21_11() {
        setPadding(Edge.ALL, 5);

        Element row = new Element();
        row.setFlexDirection(FlexDirection.ROW);
        row.addChild(new ItemElement(new ItemStack(Items.SAND), 50));
        row.addChild(new ItemElement(new ItemStack(Items.STONE_AXE), 50));
        row.addChild(new PlayerHeadElement(50));
        addChild(row);

        Element row2 = new Element();
        row2.setFlexDirection(FlexDirection.ROW);
        row2.addChild(new ItemElement(new ItemStack(Items.DIAMOND), 50));
        row2.addChild(new Text("Get item with custom tooltip") {{
            DemoWindow.addHoverBg(this);
            setAlignSelf(net.glintwein.ui.data.Align.CENTER);
            setOnClick((button) -> {
                registerTooltipFactory();
                ItemStack stack = new ItemStack(Items.DIAMOND);
                stack.set(DataComponents.CUSTOM_NAME, GlintweinTooltip.createKey("glintwein_demo", "diamond"));
                Minecraft.getInstance().player.getInventory().add(stack);
                return true;
            });
        }});
        addChild(row2);
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
