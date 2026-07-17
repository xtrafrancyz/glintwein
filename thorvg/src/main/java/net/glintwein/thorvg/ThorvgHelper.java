package net.glintwein.thorvg;

import io.github.xtrafrancyz.jthorvg.Thorvg;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThorvgHelper {
    private static ExecutorService executor;

    private static boolean loaded = false;

    public static void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        Thorvg.load();
        Thorvg.init(0);

        executor = Executors.newSingleThreadExecutor(run -> {
            Thread thread = new Thread(run);
            thread.setName("ThorVG Async Renderer");
            thread.setDaemon(true);
            return thread;
        });
    }

    public static void execute(Runnable runnable) {
        ensureLoaded();
        executor.execute(runnable);
    }
}
