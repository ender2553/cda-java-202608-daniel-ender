package com.cyberdev.dndrpg.logging;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * PROVIDED UTILITY -- general application logging (startup, shutdown,
 * unexpected errors). This is deliberately separate from AuditLogger:
 * this log is for developers/operators diagnosing the application; the
 * audit log is a security record of what players did. Mixing the two
 * makes both harder to use -- an incident responder searching the audit
 * trail should never have to wade through routine debug lines.
 */
public final class AppLogger {

    private static final Logger LOGGER = Logger.getLogger("dndrpg.app");
    private static boolean initialized = false;

    private AppLogger() {}

    public static synchronized void init() {
        if (initialized) return;
        try {
            LOGGER.setUseParentHandlers(false);

            ConsoleHandler console = new ConsoleHandler();
            console.setLevel(Level.WARNING); // keep gameplay console output clean
            LOGGER.addHandler(console);

            FileHandler file = new FileHandler("dndrpg-app.log", true);
            file.setFormatter(new SimpleFormatter());
            file.setLevel(Level.ALL);
            LOGGER.addHandler(file);

            LOGGER.setLevel(Level.ALL);
            initialized = true;
        } catch (IOException e) {
            // If the log file can't be created, fall back to console-only
            // rather than crashing the whole application over logging.
            System.err.println("Could not initialize file logging: " + e.getMessage());
        }
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warning(message);
    }

    /**
     * Logs the FULL exception detail server-side. The caller is
     * responsible for showing the user only a generic message -- see the
     * safe-error-surface pattern used throughout AuthService and
     * ConsoleUI.
     */
    public static void error(String message, Throwable t) {
        LOGGER.log(Level.SEVERE, message, t);
    }
}
