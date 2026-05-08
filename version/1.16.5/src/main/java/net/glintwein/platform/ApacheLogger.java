package net.glintwein.platform;

import org.apache.logging.log4j.LogManager;

public class ApacheLogger implements Logger {
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger("Glintwein");

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        log.warn(message, throwable);
    }

    @Override
    public void error(String message) {
        log.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }
}
