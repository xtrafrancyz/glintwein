package net.glintwein.platform;

public class PlatformProvider {
    private static Platform platform;

    public static void set(Platform platform) {
        if (PlatformProvider.platform != null)
            throw new IllegalStateException("Platform already set");
        PlatformProvider.platform = platform;
    }

    public static Platform get() {
        return platform;
    }
}
