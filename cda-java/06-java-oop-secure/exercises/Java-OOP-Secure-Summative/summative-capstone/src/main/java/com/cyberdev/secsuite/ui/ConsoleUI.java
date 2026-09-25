package com.cyberdev.secsuite.ui;

import com.cyberdev.secsuite.exception.AuthenticationException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Analyst;
import com.cyberdev.secsuite.service.AuthService;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Handles the analyst login gate and the real application's menu prompts. The SEC-17 grading
 * seam uses the same login gate with scripted input, then runs a deterministic pipeline.
 *
 * Flow: "Register a new analyst account first? [y/N]" -> optional registration (username,
 * password, contact email) -> login (username, password), at most MAX_LOGIN_ATTEMPTS tries.
 * Returns the authenticated Analyst, or throws AuthenticationException (Main exits) --
 * including on end-of-input, so a closed stdin fails CLOSED instead of looping forever or
 * falling through as "logged in".
 *
 * Input sources:
 *   - Interactive terminal (System.console() != null): passwords are read with
 *     Console.readPassword, which does not echo them.
 *   - Redirected stdin (piped answers for a non-interactive/CI run, e.g. the instructor's
 *     scripted end-to-end run): every answer, passwords included, is read line by line. Echo
 *     suppression is meaningless for piped input; a warning says so.
 * One source is chosen up front and used for every prompt: mixing Console reads with a
 * buffered stdin reader loses input that the buffer has already consumed.
 *
 * Passwords are handled as char[] and wiped (Arrays.fill) as soon as they have been used.
 */
public final class ConsoleUI {

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int MAX_REGISTRATION_ATTEMPTS = 3;
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RESET = "\u001B[0m";

    private final Console console;
    private final BufferedReader reader;
    private final PrintStream out;

    /**
     * @param console the interactive console, or null when stdin is not a terminal
     * @param reader  line reader used when console is null (e.g. wrapping System.in)
     * @param out     where prompts and messages go
     */
    public ConsoleUI(Console console, BufferedReader reader, PrintStream out) {
        if (console == null && reader == null) {
            throw new ValidationException("either a console or a reader is required");
        }
        if (out == null) {
            throw new ValidationException("out must not be null");
        }
        this.console = console;
        this.reader = reader;
        this.out = out;
    }

    public Analyst runLoginGate(AuthService authService) {
        if (authService == null) {
            throw new ValidationException("authService must not be null");
        }
        if (console == null) {
            //out.println("[info] stdin is not a terminal -- reading answers (including passwords) line by line.");
        }
        String answer = readLine(ANSI_YELLOW + "Register a new analyst account first? [y/N]: " + ANSI_RESET);
        if (answer != null && answer.strip().toLowerCase(Locale.ROOT).startsWith("y")) {
            register(authService);
        }
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            String username = readLine("Username: ");
            char[] password = readPassword("Password: ");
            if (username == null || password == null) {
                throw new AuthenticationException("Login aborted: no more input");
            }
            try {
                Analyst analyst = authService.login(username, password);
                out.println("Login successful. Welcome, " + analyst.getUsername() + ".");
                return analyst;
            } catch (AuthenticationException e) {
                out.println(e.getMessage() + " (attempt " + attempt + " of " + MAX_LOGIN_ATTEMPTS + ")");
            } finally {
                Arrays.fill(password, '\0');
            }
        }
        throw new AuthenticationException("Too many failed login attempts");
    }

    /** Professional main menu shown after successful authentication. */
    public void showMainMenu(String username) {
        out.println();
        out.println("+======================================================================+");
        out.println("|                      SECOPS ANALYST SUITE                            |");
        out.println("|                 Security Operations Workbench                       |");
        out.println("+======================================================================+");
        out.println("|                                                                      |");
        out.println("|                                                                      |");
        out.printf ("|  Analyst: %-58s|%n", safeDisplay(username));
        out.println("+----------------------------------------------------------------------+");
        out.println("|  SECURITY WORKFLOWS                                                  |");
        out.println("|                                                                      |");
        out.println("|   [1] Threat Intelligence Ingestion                                  |");
        out.println("|   [2] Vulnerability Scan                                             |");
        out.println("|   [3] Risk Register                                                  |");
        out.println("|   [4] SBOM / STRIDE / Threat Correlation                             |");
        out.println("|   [5] SQL Injection Regression Check                                 |");
        out.println("|   [6] Generate Security Assessment Report                            |");
        out.println("|   [7] Run Full Security Assessment                                   |");
        out.println("|                                                                      |");
        out.println("|   [0] Logout                                                         |");
        out.println("+======================================================================+");
    }

    /** Reads and validates a numeric menu choice without terminating the application. */
    public int readMenuChoice(int min, int max) {
        while (true) {
            String value = readLine("Select an option [" + min + "-" + max + "]: ");
            if (value == null) {
                throw new AuthenticationException("Session ended: no more input");
            }
            try {
                int choice = Integer.parseInt(value.strip());
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // Friendly validation below; never expose parsing internals to the user.
            }
            out.println("[!] Invalid selection. Enter a number from " + min + " through " + max + ".");
        }
    }

    public void showSection(String title, String subtitle) {
        out.println();
        out.println("+----------------------------------------------------------------------+");
        out.printf ("|  %-68s|%n", safeDisplay(title));
        out.println("+----------------------------------------------------------------------+");
        out.println("  " + safeDisplay(subtitle));
        out.println();
    }

    public void pause() {
        readLine("Press ENTER to return to the main menu...");
    }

    public void showLogout(String username) {
        out.println();
        out.println("+----------------------------------------------------------------------+");
        out.println("|  SESSION CLOSED                                                      |");
        out.println("+----------------------------------------------------------------------+");
        out.println("  " + safeDisplay(username) + " has been logged out securely.");
        out.println("  Thank you for using SecOps Analyst Suite.");
        out.println();
    }

    private String safeDisplay(String value) {
        if (value == null) return "";
        return value.replace("\r", " ").replace("\n", " ");
    }

    private void register(AuthService authService) {
        for (int attempt = 1; attempt <= MAX_REGISTRATION_ATTEMPTS; attempt++) {
            String username = readLine("New username (3-32 chars, a-z 0-9 . _ -): ");
            char[] password = readPassword("New password (12-128 chars): ");
            String email = readLine("Contact email: ");
            if (username == null || password == null || email == null) {
                throw new AuthenticationException("Registration aborted: no more input");
            }
            try {
                Analyst analyst = authService.register(username, password, email);
                out.println("Registered analyst '" + analyst.getUsername()
                        + "' (password stored as a PBKDF2 hash, contact email stored AES-GCM encrypted).");
                return;
            } catch (ValidationException | AuthenticationException e) {
                out.println("Registration failed: " + e.getMessage());
            } finally {
                Arrays.fill(password, '\0');
            }
        }
        out.println("Registration not completed; continuing to login.");
    }

    private String readLine(String prompt) {
        if (console != null) {
            return console.readLine("%s", prompt);
        }
        out.print(prompt);
        out.flush();
        try {
            String line = reader.readLine();
            if (line != null) {
                out.println();
            }
            return line;
        } catch (IOException e) {
            // fail closed: an unreadable stdin is a failed login, never a pass-through
            throw new AuthenticationException("Login aborted: could not read input");
        }
    }

    private char[] readPassword(String prompt) {
        if (console != null) {
            return console.readPassword("%s", prompt);
        }
        String line = readLine(prompt);
        return line == null ? null : line.toCharArray();
    }
}
