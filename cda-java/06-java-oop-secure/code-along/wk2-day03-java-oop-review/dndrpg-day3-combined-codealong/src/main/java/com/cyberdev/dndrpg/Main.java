package com.cyberdev.dndrpg;

import com.cyberdev.dndrpg.config.ApplicationConfig;
import com.cyberdev.dndrpg.logging.AppLogger;
import com.cyberdev.dndrpg.ui.ConsoleUI;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Starts the Spring application context and delegates to the console UI.
 * The default profile uses file-backed game data and in-memory repositories.
 * Activate the "jdbc" profile to use the PostgreSQL implementations.
 */
public final class Main {
    public static void main(String[] args) {
        AppLogger.init();

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(ApplicationConfig.class)) {
            context.getBean(ConsoleUI.class).run();
        } catch (Exception e) {
            AppLogger.error("Unhandled error -- shutting down", e);
            System.out.println("A fatal error occurred. Check dndrpg-app.log for details.");
        }
    }
}
