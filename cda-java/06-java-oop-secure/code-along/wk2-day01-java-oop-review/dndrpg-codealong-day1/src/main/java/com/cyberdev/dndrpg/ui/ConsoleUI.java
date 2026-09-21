package com.cyberdev.dndrpg.ui;

import com.cyberdev.dndrpg.model.CharacterClass;
import com.cyberdev.dndrpg.model.ClassDefinition;
import com.cyberdev.dndrpg.model.PlayerCharacter;
import com.cyberdev.dndrpg.service.GameService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * DAY 1 SNAPSHOT -- all console input/output lives here so the rest of
 * the application never touches System.in/System.out directly.
 *
 * There is no login/registration here yet: player accounts (Player,
 * AuthService, password hashing) don't exist in the codebase until
 * later in the series, so a "player" today is just a UUID generated on
 * the spot -- enough to give each PlayerCharacter a playerId without
 * pulling in material we haven't covered.
 */
public final class ConsoleUI {

    private final GameService gameService;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(GameService gameService) {
        this.gameService = gameService;
    }

    public void run() {
        System.out.println("=== Dungeon Crawler (Day 1 build) ===");
        int playerCount = askInt("How many players (1-4)? ", 1, 4);

        List<PlayerCharacter> party = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            System.out.println("\n-- Player " + i + " --");
            party.add(createCharacter());
        }

        int rounds = gameService.roundsPerRun();
        for (int round = 1; round <= rounds; round++) {
            System.out.println("\n=== Round " + round + " of " + rounds + " ===");
            for (PlayerCharacter character : party) {
                if (character.isDefeated()) continue;
                System.out.println("\n" + character.getDisplayName() + "'s turn:");
                gameService.runEncounter(character);
            }
        }

        System.out.println("\n=== The party has reached the boss chamber! ===");
        PlayerCharacter champion = party.stream()
                .filter(c -> !c.isDefeated())
                .max((a, b) -> Integer.compare(a.getScore(), b.getScore()))
                .orElse(party.get(0));
        System.out.println(champion.getDisplayName() + " steps forward to face the boss.");
        boolean bossDefeated = gameService.runBossEncounter(champion);
        System.out.println(bossDefeated
                ? champion.getDisplayName() + " has defeated the boss!"
                : "The party was defeated by the boss...");

        System.out.println("\n=== Final party ===");
        for (PlayerCharacter c : party) {
            System.out.println("  " + c.getDisplayName() + " - level " + c.getLevel()
                    + ", " + c.getGold() + " gold, score " + c.getScore());
        }
    }

    private PlayerCharacter createCharacter() {
        // No account system yet -- just a fresh id to identify "whose"
        // character this is for the rest of the session.
        UUID playerId = UUID.randomUUID();

        System.out.print("Character name: ");
        String name = scanner.nextLine().trim();
        List<ClassDefinition> classes = gameService.availableClasses();
        System.out.println("Choose a class:");
        for (int i = 0; i < classes.size(); i++) {
            ClassDefinition c = classes.get(i);
            System.out.println("  " + (i + 1) + ". " + c.characterClass() + " - " + c.description());
        }
        int choice = askInt("Choice: ", 1, classes.size());
        CharacterClass characterClass = classes.get(choice - 1).characterClass();
        return gameService.createCharacter(playerId, name, characterClass);
    }

    private int askInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
                // fall through to the retry message below
            }
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }
}
