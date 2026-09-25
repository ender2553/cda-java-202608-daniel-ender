package com.example.rpg.ui;

import com.example.rpg.domain.PlayerCharacter;
import com.example.rpg.domain.UserAccount;
import com.example.rpg.service.AdventureService;
import com.example.rpg.service.AuthService;
import java.util.Locale;
import java.util.Scanner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** CLI adapter; it receives services through constructor injection and contains no SQL or hashing logic. */
@Component
public final class RpgConsole implements CommandLineRunner {
    private final AuthService auth;
    private final AdventureService adventure;

    public RpgConsole(AuthService auth, AdventureService adventure) {
        this.auth = auth;
        this.adventure = adventure;
    }

    @Override
    public void run(String... args) {
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("=== RPG Adventure ===");
            UserAccount account = authenticate(input);
            if (account == null) {
                return;
            }

            PlayerCharacter player = adventure.loadOrCreate(account.id(), account.username());
            System.out.println("Welcome " + player.name() + "!");
            printCommands();

            while (true) {
                String command = readLine(input, "> ");
                if (command == null) {
                    adventure.save(player);
                    System.out.println("\nInput closed. Progress saved.");
                    return;
                }
                switch (command.trim().toLowerCase(Locale.ROOT)) {
                    case "explore" -> System.out.println(adventure.explore(player).summary());
                    case "battle" -> System.out.println(adventure.battle(player).summary());
                    case "status" -> printStatus(player);
                    case "save" -> {
                        adventure.save(player);
                        System.out.println("Progress saved.");
                    }
                    case "help" -> printCommands();
                    case "quit", "exit", "end" -> {
                        adventure.save(player);
                        System.out.println("Progress saved. Thanks for playing!");
                        return;
                    }
                    case "" -> {
                    }
                    default -> printCommands();
                }
            }
        }
    }

    private UserAccount authenticate(Scanner input) {
        while (true) {
            String username = readLine(input, "Username (or quit): ");
            if (username == null) {
                return null;
            }
            username = username.trim();
            if (isExitCommand(username)) {
                return null;
            }

            String password = readLine(input, "Password: ");
            if (password == null) {
                return null;
            }

            try {
                return auth.login(username, password);
            } catch (IllegalArgumentException loginFailure) {
                String register = readLine(input, "Login failed. Register this username? (y/n): ");
                if (register == null) {
                    return null;
                }
                if (!register.trim().equalsIgnoreCase("y")) {
                    continue;
                }

                try {
                    return auth.register(username, password);
                } catch (IllegalArgumentException registrationFailure) {
                    System.out.println("Registration failed: " + registrationFailure.getMessage());
                }
            }
        }
    }

    private String readLine(Scanner input, String prompt) {
        System.out.print(prompt);
        System.out.flush();
        return input.hasNextLine() ? input.nextLine() : null;
    }

    private boolean isExitCommand(String command) {
        return command.equalsIgnoreCase("quit")
                || command.equalsIgnoreCase("exit")
                || command.equalsIgnoreCase("end");
    }

    private void printStatus(PlayerCharacter player) {
        System.out.println(
                "Level " + player.level()
                        + " | HP " + player.health() + "/" + player.maxHealth()
                        + " | Gold " + player.gold()
                        + " | Location " + player.location()
                        + " | Items " + player.inventory().size());
    }

    private void printCommands() {
        System.out.println("Commands: explore, battle, status, save, help, quit");
    }
}
