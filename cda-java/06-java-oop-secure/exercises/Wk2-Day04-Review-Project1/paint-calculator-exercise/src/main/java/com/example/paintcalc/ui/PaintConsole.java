package com.example.paintcalc.ui;

import com.example.paintcalc.domain.*;
import com.example.paintcalc.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Scanner;

/** Console adapter: presentation logic stays here instead of leaking into domain classes. */
@Component
public final class PaintConsole implements CommandLineRunner {
    private static final PaintProduct STANDARD_PAINT = new PaintProduct("Everyday Interior", new BigDecimal("38.50"), new BigDecimal("350"));
    private final AuthService auth;
    private final PaintApplicationService paint;
    private final ColorAdvisor advisor;

    public PaintConsole(AuthService auth, PaintApplicationService paint, ColorAdvisor advisor) { this.auth = auth; this.paint = paint; this.advisor = advisor; }

    public void run(String... args) {
        // The application remains active until the user selects Q/quit.
        try (Scanner in = new Scanner(System.in)) {
            banner();
            UserAccount user = authenticate(in);
            while (true) {
                System.out.println("\n[1] Paint estimate  [2] Color advisor  [3] Estimate history  [Q] Exit");
                String command = in.nextLine().trim().toLowerCase();
                try {
                    switch (command) {
                        case "1" -> estimate(in, user);
                        case "2" -> advise(in);
                        case "3" -> history(user);
                        case "q", "quit" -> { System.out.println("Paint on, adventurer! 🎨"); return; }
                        default -> System.out.println("Please choose 1, 2, 3, or Q.");
                    }
                } catch (IllegalArgumentException ex) { System.out.println("Input problem: " + ex.getMessage()); }
            }
        }
    }

    private UserAccount authenticate(Scanner in) {
        System.out.print("Username: "); String username = in.nextLine();
        System.out.print("Password: "); String password = in.nextLine();
        try { return auth.login(username, password); }
        catch (IllegalArgumentException ex) {
            System.out.print("Login failed. Register this username? (y/n): ");
            if (!in.nextLine().equalsIgnoreCase("y")) throw ex;
            return auth.register(username, password);
        }
    }

    private void estimate(Scanner in, UserAccount user) {
        BigDecimal length = decimal(in, "Room length in feet: ");
        BigDecimal width = decimal(in, "Room width in feet: ");
        BigDecimal height = decimal(in, "Room height in feet: ");
        int coats = integer(in, "Number of coats (1-3): ");
        PaintColor color = color(in);
        PaintEstimate result = paint.estimate(user.id(), new Room(length, width, height), coats, color, STANDARD_PAINT);
        System.out.printf("\nYou need about %d gallon(s) of %s.%n", result.gallons(), color.displayName());
        System.out.printf("Paint cost estimate: $%s%n", result.cost());
        System.out.println("Paint meter: " + "█".repeat(Math.min(result.gallons(), 20)) + " 🪣");
    }

    private void advise(Scanner in) {
        PaintColor color = color(in);
        System.out.print("Is the room small? (y/n): "); boolean small = in.nextLine().equalsIgnoreCase("y");
        System.out.print("Does it have low natural light? (y/n): "); boolean dark = in.nextLine().equalsIgnoreCase("y");
        System.out.println(advisor.advise(color, small, dark).message());
    }

    private void history(UserAccount user) {
        var history = paint.history(user.id());
        if (history.isEmpty()) System.out.println("No estimates saved yet.");
        history.forEach(e -> System.out.printf("%s | %d gallon(s) | $%s | %d coat(s)%n", e.color().displayName(), e.gallons(), e.cost(), e.coats()));
    }

    private PaintColor color(Scanner in) {
        PaintColor[] colors = PaintColor.values();
        for (int i = 0; i < colors.length; i++) System.out.printf("%d. %s%n", i + 1, colors[i].displayName());
        int selected = integer(in, "Choose a color: ");
        if (selected < 1 || selected > colors.length) throw new IllegalArgumentException("unknown color");
        return colors[selected - 1];
    }
    private BigDecimal decimal(Scanner in, String prompt) { System.out.print(prompt); return new BigDecimal(in.nextLine().trim()); }
    private int integer(Scanner in, String prompt) { System.out.print(prompt); return Integer.parseInt(in.nextLine().trim()); }
    private void banner() { System.out.println("\n  ____       _       _     ___                  _   "); System.out.println(" |  _ \\ __ _(_)_ __ | |_  / _ \\ _   _  ___  ___| |_ "); System.out.println(" | |_) / _` | | '_ \\| __|| | | | | | |/ _ \\/ __| __|"); System.out.println(" |  __/ (_| | | | | | |_ | |_| | |_| |  __/\\__ \\ |_ "); System.out.println(" |_|   \\__,_|_|_| |_|\\__| \\__\\_\\\\__,_|\\___||___/\\__|"); System.out.println("\n                 PAINT QUEST: COVER YOUR ROOM 🎨\n"); }
}
