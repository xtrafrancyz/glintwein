package net.glintwein.devtest;

import net.glintwein.Glintwein;

public class DevTest {
    public static void init() {
        Glintwein.instance.layerIngame.getWindowManager().addWindow(new TestWindow());
    }
}
