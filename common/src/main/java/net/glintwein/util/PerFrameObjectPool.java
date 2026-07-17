package net.glintwein.util;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PerFrameObjectPool<T> {
    private static final List<PerFrameObjectPool<?>> ALL_POOLS = new CopyOnWriteArrayList<>();

    public static void onFrameEndAllPools() {
        for (PerFrameObjectPool<?> pool : ALL_POOLS) {
            pool.onFrameEnd();
        }
    }

    private final Supplier<T> factory;
    private final Consumer<T> resetter;

    private final ArrayDeque<T> free = new ArrayDeque<>();
    private int usage;
    private int peakUsage;
    private final int[] usageHistory = new int[40];
    private int historyIndex;

    public PerFrameObjectPool(Supplier<T> factory) {
        this(factory, ignored -> {
        });
    }

    public PerFrameObjectPool(Supplier<T> factory, Consumer<T> resetter) {
        this.factory = factory;
        this.resetter = resetter;
        ALL_POOLS.add(this);
    }

    public T acquire() {
        usage++;
        if (usage > peakUsage) {
            peakUsage = usage;
        }
        T obj = free.isEmpty() ? factory.get() : free.pop();
        return obj;
    }

    public void release(T obj) {
        if (usage == 0)
            return;
        usage--;
        resetter.accept(obj);
        free.push(obj);
    }

    private void onFrameEnd() {
        usageHistory[historyIndex] = peakUsage;
        historyIndex = (historyIndex + 1) % usageHistory.length;
        usage = 0;
        peakUsage = 0;

        int targetFreeSize = computeTargetFreeSize();
        while (free.size() > targetFreeSize)
            free.pop();
    }

    private int computeTargetFreeSize() {
        int sum = 0;
        int count = 0;
        for (int j : usageHistory) {
            sum += j;
            if (j > 0)
                count++;
        }
        if (count == 0)
            return 1;
        int avg = sum / count;
        // add 20% to the average usage to avoid frequent resizing
        return (int) (avg * 1.2);
    }
}
