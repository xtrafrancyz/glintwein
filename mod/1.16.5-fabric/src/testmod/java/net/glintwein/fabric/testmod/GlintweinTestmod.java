package net.glintwein.fabric.testmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.glintwein.demo.DemoWindow;
import net.glintwein.thorvg.Lottie;
import net.glintwein.thorvg.SVGImage;
import net.glintwein.ui.GlintweinTooltip;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.element.Text;
import net.glintwein.util.ResourceLoaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GlintweinTestmod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DemoWindow.addCustomInitializer(demo -> {
            demo.root.addChild(new DemoWindow.Collapse("ThorVG", new ThorVGDemo()));
        });

        GlintweinTooltip.register("glintwein_testmod", data -> {
            if (data.equals("hello")) {
                return new Text("Hello, Glintwein!");
            }
            return null;
        });

        ClientCommandManager.DISPATCHER.register(
            ClientCommandManager.literal("hello").executes(context -> {
                ItemStack is = new ItemStack(Items.MUSIC_DISC_11, 1);
                is.setHoverName(GlintweinTooltip.createKey("glintwein_testmod", "hello"));
                Minecraft.getInstance().player.inventory.add(is);
                return 0;
            })
        );
    }

    private static class ThorVGDemo extends Element {
        public ThorVGDemo() {
            setSize(300, 200);

            Lottie catElem = new Lottie(ResourceLoaderUtil.toBytes(
                GlintweinTestmod.class.getResourceAsStream("/assets/lottie_cat.json")
            ));
            catElem.setWidthPercent(50);
            addChild(catElem);

            SVGImage svgElem = new SVGImage("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 130 77\">\n" +
                "  <path d=\"M2,12l8-6h11v11l-6,8zM62,12l8-6h11v11l-6,8zM2,62l8-6h11v11l-6,8zM62,62l8-6h11v11l-6,8z\" fill=\"#fe898b\"/>\n" +
                "  <path d=\"M2,12h13v13h-13zM62,12h13v13h-13zM2,62h13v13h-13zM62,62h13v13h-13z\" fill=\"#cb0612\"/>\n" +
                "\n" +
                "  <path d=\"M23,12l8-6h29v11l-5,7h-4v9l-6,7zM59,68l-5,6l-30-11l6-7h3v-8l6-5h11v14h9z\" fill=\"#52a9ff\"/>\n" +
                "  <path d=\"M23,12h32v12h-10v16h-12v-16h-10zM54,74h-30v-11h9v-15h12v15h9z\" fill=\"#5c64b5\"/>\n" +
                "\n" +
                "  <path d=\"M84,12l8-6c18-4,38,19,34,27l-5,6zM84,63c18,4,38,5,42-21h-12l-5,6c-2,14,-18,10-20,10z\" fill=\"#87f7a2\"/>\n" +
                "  <path d=\"M84,12c20-5,41,15,37,27h-12c0-12-8-15-25-15zM84,75c20,3,41-15,37-27h-12c0,12-8,15-25,15z\" fill=\"#18bf73\"/>\n" +
                "</svg>\n");
            svgElem.setWidthPercent(50);
            svgElem.setPosition(Edge.TOP, 0);
            svgElem.setPosition(Edge.RIGHT, 0);
            svgElem.setPositionType(PositionType.ABSOLUTE);
            addChild(svgElem);
        }
    }
}
