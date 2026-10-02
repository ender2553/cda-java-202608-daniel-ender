package demo.ledger.console;

import demo.ledger.domain.HolderSummary;
import demo.ledger.repository.AccountHolderRepository;
import demo.ledger.repository.AccountRepository;
import demo.ledger.repository.DataAccessFailure;
import demo.ledger.service.TransferService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * The view: a numeric menu. It only reads input, calls a repository or
 * service, and prints the result — it never touches SQL or a Connection.
 */
@Component
public class ConsoleMenu implements CommandLineRunner {

    private final AccountHolderRepository holders;
    private final AccountRepository accounts;
    private final TransferService transfers;
    private final Scanner in = new Scanner(System.in);

    public ConsoleMenu(AccountHolderRepository holders, AccountRepository accounts, TransferService transfers) {
        this.holders = holders;
        this.accounts = accounts;
        this.transfers = transfers;
    }

    @Override
    public void run(String... args) {
        while (true) {
            System.out.println("""

                    === LedgerCore Demo ===
                    1) List account holders
                    2) Search holders by name
                    3) View holder details
                    4) Add account holder
                    5) Transfer funds
                    6) Show account balances
                    0) Exit""");
            String choice = prompt("Choose");
            if (choice == null || choice.equals("0")) {
                return;
            }
            try {
                switch (choice) {
                    case "1" -> printSummaries(holders.findAll(prompt("Sort by (account_id, display_name, created_at)")));
                    case "2" -> printSummaries(holders.searchByName(prompt("Name contains")));
                    case "3" -> holders.findById(prompt("Account ID")).ifPresentOrElse(
                            h -> System.out.printf("%s | %s | tax ID %s | since %s%n",
                                    h.accountId(), h.displayName(), h.maskedTaxId(), h.createdAt().toLocalDate()),
                            () -> System.out.println("No holder for that account."));
                    case "4" -> {
                        holders.create(prompt("Account ID"), prompt("Display name"), prompt("Tax ID (123-45-6789)"));
                        System.out.println("Holder saved. Tax ID stored encrypted.");
                    }
                    case "5" -> {
                        transfers.transfer(prompt("From account"), prompt("To account"),
                                parseAmount(prompt("Amount")), prompt("Memo (optional)"));
                        System.out.println("Transfer complete.");
                    }
                    case "6" -> accounts.findAll().forEach(a ->
                            System.out.printf("%s  %12s%n", a.accountId(), a.balance()));
                    default -> System.out.println("Please choose 0-6.");
                }
            } catch (IllegalArgumentException | DataAccessFailure ex) {
                // Both carry messages written to be shown to a user; nothing internal.
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void printSummaries(List<HolderSummary> list) {
        if (list.isEmpty()) {
            System.out.println("No holders found.");
        }
        list.forEach(h -> System.out.printf("%s  %-30s  since %s%n",
                h.accountId(), h.displayName(), h.createdAt().toLocalDate()));
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    private static BigDecimal parseAmount(String text) {
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("Amount must be a number like 25.00");
        }
    }
}
