package net.glintwein.ui.util;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NativeCleaner {
    private static final ReferenceQueue<Handle> REF_QUEUE = new ReferenceQueue<>();
    private static final Set<Ref> REFERENCES = ConcurrentHashMap.newKeySet();

    public static Handle register(Runnable cleanupAction) {
        Handle handle = new Handle();
        Ref ref = new Ref(handle, cleanupAction);
        REFERENCES.add(ref);
        return handle;
    }

    public static void cleanUp() {
        Ref ref;
        while ((ref = (Ref) REF_QUEUE.poll()) != null) {
            ref.clean();
            REFERENCES.remove(ref);
        }
    }

    public static class Handle {
        // Empty handle class
    }

    private static class Ref extends PhantomReference<Handle> {
        private final Runnable cleanupAction;

        Ref(Handle referent, Runnable cleanupAction) {
            super(referent, REF_QUEUE);
            this.cleanupAction = cleanupAction;
        }

        public void clean() {
            cleanupAction.run();
        }
    }
}
