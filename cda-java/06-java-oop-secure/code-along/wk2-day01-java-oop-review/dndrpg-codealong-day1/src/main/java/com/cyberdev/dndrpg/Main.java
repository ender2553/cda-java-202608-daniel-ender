package com.cyberdev.dndrpg;

import com.cyberdev.dndrpg.logging.AppLogger;
import com.cyberdev.dndrpg.repository.CharacterRepository;
import com.cyberdev.dndrpg.repository.GameDataRepository;
import com.cyberdev.dndrpg.repository.file.FileGameDataRepository;
import com.cyberdev.dndrpg.repository.inmemory.InMemoryCharacterRepository;
import com.cyberdev.dndrpg.service.CombatEngine;
import com.cyberdev.dndrpg.service.GameService;
import com.cyberdev.dndrpg.ui.ConsoleUI;

/**
 * DAY 1 code-along composition root: the ONLY place that chooses
 * concrete implementations. Everything else here depends on an
 * interface and gets its collaborators handed to it via the
 * constructor (constructor injection).
 *
 * Both repositories wired in today are the simplest possible kind:
 * FileGameDataRepository reads CSV files off the classpath, and
 * InMemoryCharacterRepository is a ConcurrentHashMap. Day 2 adds a
 * JDBC-backed alternative for each one that satisfies the exact same
 * interface -- nothing in GameService or ConsoleUI will need to change
 * for that swap to work.
 */
public final class Main {
    public static void main(String[] args) {
        AppLogger.init();

        GameDataRepository gameDataRepository = new FileGameDataRepository();
        CharacterRepository characterRepository = new InMemoryCharacterRepository();
        CombatEngine combatEngine = new CombatEngine();
        GameService gameService = new GameService(gameDataRepository, characterRepository, combatEngine);

        ConsoleUI ui = new ConsoleUI(gameService);
        try {
            ui.run();
        } catch (Exception e) {
            AppLogger.error("Unhandled error -- shutting down", e);
            System.out.println("A fatal error occurred. Check dndrpg-app.log for details.");
        }
    }
}
