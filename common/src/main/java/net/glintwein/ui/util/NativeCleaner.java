package net.glintwein.ui.util;

import net.glintwein.platform.Platform;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NativeCleaner {
    private static final ReferenceQueue<Object> REF_QUEUE = new ReferenceQueue<>();
    private static final Set<Ref> REFERENCES = ConcurrentHashMap.newKeySet();

    public static void register(Object referent, Runnable cleanupAction) {
        Ref ref = new Ref(referent, cleanupAction);
        REFERENCES.add(ref);
    }

    public static void cleanUp() {
        Ref ref;
        while ((ref = (Ref) REF_QUEUE.poll()) != null) {
            try {
                ref.clean();
            } catch (Exception e) {
                Platform.log().error("Error during native cleanup", e);
            } finally {
                REFERENCES.remove(ref);
            }
        }
    }

    private static class Ref extends PhantomReference<Object> {
        private final Runnable cleanupAction;

        Ref(Object referent, Runnable cleanupAction) {
            super(referent, REF_QUEUE);
            this.cleanupAction = cleanupAction;
        }

        public void clean() {
            cleanupAction.run();
        }
    }
}
