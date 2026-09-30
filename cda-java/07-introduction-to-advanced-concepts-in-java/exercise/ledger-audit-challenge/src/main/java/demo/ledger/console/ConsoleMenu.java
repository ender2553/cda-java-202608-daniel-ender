package demo.ledger.console;

import demo.ledger.domain.AccountHolder;
import demo.ledger.repository.AccountHolderRepository;
import demo.ledger.repository.AccountRepository;
import demo.ledger.repository.TransactionRepository;
import demo.ledger.service.TransferService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class ConsoleMenu implements CommandLineRunner {

    private final AccountHolderRepository holders;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final TransferService transfers;
    private final Scanner in = new Scanner(System.in);

    public ConsoleMenu(AccountHolderRepository holders, AccountRepository accounts,
                       TransactionRepository transactions, TransferService transfers) {
        this.holders = holders;
        this.accounts = accounts;
        this.transactions = transactions;
        this.transfers = transfers;
    }

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            System.out.println("""

                    === LedgerCore ===
                    1) List account holders
                    2) Search holders by name
                    3) View holder details
                    4) Add account holder
                    5) Delete account holder
                    6) Transfer funds
                    7) Bulk transfer
                    8) Show account balances
                    9) Transaction history
                    0) Exit""");
            String choice = prompt("Choose");
            if (choice.equals("0")) {
                return;
            }
            try {
                switch (choice) {
                    case "1" -> print(holders.findAll(prompt("Sort by")));
                    case "2" -> print(holders.searchByName(prompt("Name contains")));
                    case "3" -> System.out.println(holders.findById(prompt("Account ID")));
                    case "4" -> {
                        holders.create(prompt("Account ID"), prompt("Display name"), prompt("Tax ID"));
                        System.out.println("Holder saved.");
                    }
                    case "5" -> System.out.println(holders.delete(prompt("Account ID")) + " holder(s) deleted.");
                    case "6" -> {
                        transfers.transfer(prompt("From account"), prompt("To account"),
                                Double.parseDouble(prompt("Amount")), prompt("Memo"));
                        System.out.println("Transfer complete.");
                    }
                    case "7" -> bulkTransfer();
                    case "8" -> accounts.findAll().forEach(a ->
                            System.out.println(a.accountId() + "  " + a.balance()));
                    case "9" -> transactions.findByAccount(prompt("Account ID")).forEach(System.out::println);
                    default -> System.out.println("Unknown option.");
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
    }

    private void bulkTransfer() throws InterruptedException {
        System.out.println("Enter transfers as from,to,amount,memo. Blank line to run.");
        List<String[]> batch = new ArrayList<>();
        String line;
        while (!(line = prompt(">")).isEmpty()) {
            batch.add(line.split(","));
        }
        int before = transfers.getTransferCount();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (String[] t : batch) {
            pool.submit(() -> transfers.transfer(t[0], t[1], Double.parseDouble(t[2]), t.length > 3 ? t[3] : ""));
        }
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println((transfers.getTransferCount() - before) + " transfer(s) processed.");
    }

    private void print(List<AccountHolder> list) {
        list.forEach(h -> System.out.println(h.accountId() + "  " + h.displayName() + "  " + h.taxId() + "  " + h.createdAt()));
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return in.nextLine();
    }
}
