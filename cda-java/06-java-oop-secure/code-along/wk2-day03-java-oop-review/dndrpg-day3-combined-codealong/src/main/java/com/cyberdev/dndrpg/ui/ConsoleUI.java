package com.cyberdev.dndrpg.ui;

import com.cyberdev.dndrpg.event.AttackEvent;
import com.cyberdev.dndrpg.event.BossDefeatedEvent;
import com.cyberdev.dndrpg.event.DefeatEvent;
import com.cyberdev.dndrpg.event.GameEvent;
import com.cyberdev.dndrpg.event.ItemFoundEvent;
import com.cyberdev.dndrpg.event.LevelUpEvent;
import com.cyberdev.dndrpg.exception.AuthenticationException;
import com.cyberdev.dndrpg.model.CharacterClass;
import com.cyberdev.dndrpg.model.ClassDefinition;
import com.cyberdev.dndrpg.model.Player;
import com.cyberdev.dndrpg.model.PlayerCharacter;
import com.cyberdev.dndrpg.repository.LeaderboardRepository;
import com.cyberdev.dndrpg.security.AuthService;
import com.cyberdev.dndrpg.service.GameService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * DAY 3 + DAY 4 SNAPSHOT.
 *
 * Day 3: combat narration is no longer printed inline by GameService/
 * CombatEngine -- run() now calls gameService.runEncounter/runBossEncounter,
 * gets back a List&lt;GameEvent&gt;, and prints each one through an
 * exhaustive switch (printEvent below) with NO default branch. The
 * compiler, not a runtime check, guarantees every GameEvent case is
 * handled.
 *
 * Day 4: login/registration has arrived. Before any character creation,
 * each player is asked to register a new account or log in to an
 * existing one via AuthService; the authenticated Player's real getId()
 * (not a fresh UUID.randomUUID() per character, like Day 1/2/3 did) is
 * what gets passed into gameService.createCharacter(...).
 *
 * THE LOGIN GATE (design call, documented here): a failed login does not
 * end the whole session or throw the player out immediately -- it loops,
 * re-prompting for username/password, until either a login succeeds or
 * the player chooses to register instead. What it NEVER does is let a
 * failed login fall through to character creation; there is no path from
 * "login failed" to "you now have a character" for that player. This
 * mirrors the afternoon POS material's login gate in spirit (a failed
 * authentication blocks forward progress), but blocks CHARACTER CREATION
 * here instead of a checkout -- see the CODEALONG_GUIDE's "Where this
 * diverges" section.
 */
@Component
public final class ConsoleUI {

    private final GameService gameService;
    private final AuthService authService;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(GameService gameService, AuthService authService) {
        this.gameService = gameService;
        this.authService = authService;
    }

    public void run() {
        System.out.println("=== Dungeon Crawler Arena (Day 3 + Day 4 code-along build) ===");
        int playerCount = askInt("How many players (1-4)? ", 1, 4);

        List<PlayerCharacter> party = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            System.out.println("\n-- Player " + i + " --");
            Player player = authenticatePlayer();
            party.add(createCharacter(player));
        }

        int rounds = gameService.roundsPerRun();
        for (int round = 1; round <= rounds; round++) {
            System.out.println("\n=== Round " + round + " of " + rounds + " ===");
            for (PlayerCharacter character : party) {
                if (character.isDefeated()) continue;
                System.out.println("\n" + character.getDisplayName() + "'s turn:");
                for (GameEvent event : gameService.runEncounter(character)) {
                    printEvent(event);
                }
            }
        }

        System.out.println("\n=== The party has reached the boss chamber! ===");
        PlayerCharacter champion = party.stream()
                .filter(c -> !c.isDefeated())
                .max((a, b) -> Integer.compare(a.getScore(), b.getScore()))
                .orElse(party.get(0));
        System.out.println(champion.getDisplayName() + " steps forward to face the boss.");
        for (GameEvent event : gameService.runBossEncounter(champion)) {
            printEvent(event);
        }

        gameService.finalizeSession(party);
        printLeaderboard();
    }

    /**
     * Prompts once for register-or-login, then loops on login failure
     * until either a login succeeds or the player registers a brand new
     * account. Never returns without a real, authenticated Player.
     */
    private Player authenticatePlayer() {
        while (true) {
            System.out.print("Register (r) or log in (l)? ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.startsWith("r")) {
                System.out.print("Choose a username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Choose a password: ");
                String password = scanner.nextLine();
                System.out.print("Email: ");
                String email = scanner.nextLine().trim();
                try {
                    Player player = authService.register(username, password, email);
                    System.out.println("Registered! Welcome, " + player.getUsername() + ".");
                    return player;
                } catch (AuthenticationException e) {
                    System.out.println("Registration failed: " + e.getMessage() + " Try again.");
                }
            } else if (choice.startsWith("l")) {
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                try {
                    Player player = authService.login(username, password);
                    System.out.println("Welcome back, " + player.getUsername() + ".");
                    return player;
                } catch (AuthenticationException e) {
                    // THE LOGIN GATE: a failed login never falls through to
                    // character creation. Loop back to the register-or-login
                    // prompt instead of returning anything.
                    System.out.println("Login failed: invalid username or password. Try again.");
                }
            } else {
                System.out.println("Please enter 'r' to register or 'l' to log in.");
            }
        }
    }

    private PlayerCharacter createCharacter(Player player) {
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
        return gameService.createCharacter(player.getId(), name, characterClass);
    }

    /**
     * Exhaustive switch over the sealed GameEvent hierarchy -- deliberately
     * no `default` branch. If a new GameEvent subtype is ever added to the
     * permits list, this method fails to COMPILE until a case is added
     * here, rather than silently printing nothing for it at runtime.
     */
    private void printEvent(GameEvent event) {
        switch (event) {
            case AttackEvent e when e.hit() ->
                    System.out.println("  " + e.attackerName() + " hits " + e.defenderName()
                            + " for " + e.damage() + " damage! (" + e.defenderHpRemaining() + " HP remaining)");
            case AttackEvent e ->
                    System.out.println("  " + e.attackerName() + " attacks " + e.defenderName() + " and misses!");
            case DefeatEvent e ->
                    System.out.println("  " + e.defeatedName() + " was defeated by " + e.defeatedByName() + "...");
            case LevelUpEvent e ->
                    System.out.println("  " + e.characterName() + " leveled up to level " + e.newLevel() + "!");
            case ItemFoundEvent e ->
                    System.out.println("  " + e.characterName() + " found an item: " + e.itemName() + "!");
            case BossDefeatedEvent e ->
                    System.out.println("  " + e.characterName() + " has defeated the boss: " + e.bossName() + "!");
        }
    }

    private void printLeaderboard() {
        System.out.println("\n=== Leaderboard ===");
        List<LeaderboardRepository.LeaderboardEntry> top = gameService.leaderboard();
        int rank = 1;
        for (LeaderboardRepository.LeaderboardEntry entry : top) {
            System.out.println(rank++ + ". " + entry.characterName() + " - " + entry.score() + " points");
        }
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
