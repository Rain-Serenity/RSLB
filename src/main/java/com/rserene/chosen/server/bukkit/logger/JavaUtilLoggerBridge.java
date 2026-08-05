package com.rserene.chosen.server.bukkit.logger;

import com.rserene.chosen.server.api.internal.logger.Level;
import com.rserene.chosen.server.api.internal.logger.bridges.BaseLoggerBridge;
import java.util.logging.Logger;

public class JavaUtilLoggerBridge extends BaseLoggerBridge {
    private final Logger logger;

    public JavaUtilLoggerBridge(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        java.util.logging.Level jLevel;
        switch (level) {
            case DEBUG:
                jLevel = java.util.logging.Level.FINE;
                break;
            case INFO:
                jLevel = java.util.logging.Level.INFO;
                break;
            case WARN:
                jLevel = java.util.logging.Level.WARNING;
                break;
            case ERROR:
                jLevel = java.util.logging.Level.SEVERE;
                break;
            default:
                jLevel = java.util.logging.Level.INFO;
        }
        if (throwable != null) {
            this.logger.log(jLevel, message, throwable);
        } else {
            this.logger.log(jLevel, message);
        }
    }
}
