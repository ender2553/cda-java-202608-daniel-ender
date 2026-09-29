package com.rti.ledgercore.app;

import com.rti.ledgercore.crypto.FieldCipher;
import com.rti.ledgercore.db.Database;
import com.rti.ledgercore.domain.Account;
import com.rti.ledgercore.domain.AccountHolder;
import com.rti.ledgercore.domain.TransactionRecord;
import com.rti.ledgercore.logs.TransactionLogProcessor;
import com.rti.ledgercore.repository.LedgerRepository;
import com.rti.ledgercore.transactions.TransferService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/**
 * Ties the LedgerCore starter components together end to end: connects to
 * PostgreSQL (settings from {@code .env}), which seeds two accounts from
 * {@code data.sql}, opens a third, transfers funds between the seeded two,
 * stores an account holder's tax ID, reads everything back through the
 * repository, and processes a small transaction log.
 *
 * <p>This starter compiles and runs the happy path, but every component it
 * wires up is the insecure baseline the module's labs fix — see the
 * {@code // TODO (lab): ...} comments in {@code transactions.TransferService},
 * {@code crypto.FieldCipher}, {@code repository.LedgerRepository}, and
 * {@code logs.TransactionLogProcessor}.
 */
public final class LedgerCoreApp {

    public static void main(String[] args) throws SQLException, IOException {
        Database database = Database.connect(); // DB_URL / DB_USERNAME / DB_PASSWORD from .env

        // TODO (lab) Step 1 of 11 — Give the cipher a TEMPORARY 32-byte key so the
        //   AES round trip can run (Step 3 will reject the empty key below).
        //   Replace new byte[0] with:
        //     "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)
        //   INSECURE ON PURPOSE: a key in source code ends up in Git history and in
        //   the built jar. Step 10 removes it. Then go to crypto.FieldCipher, Step 2.
        //
        // TODO (lab) Step 10 of 11 — Externalize the key (after Steps 2–9 work).
        //   - Delete the literal key from Step 1.
        //   - Use FieldCipher.fromEnvironment() instead. It reads LEDGERCORE_DATA_KEY
        //     from the environment or .env (make one with: openssl rand -base64 32).
        //   - Now the key is supplied when the app runs — it is never in source,
        //     Git, or the jar.
        //
        // TODO (lab) Step 11 of 11 — Prove it works.
        //   - Run this app: "Holder on file" should still print the real tax ID.
        //   - Query Postgres directly:
        //       SELECT account_id, tax_id_encrypted FROM ledgercore.account_holders;
        //     The value must be unreadable Base64, not 123-45-6789.
        //   - Run twice: the stored value changes every run (fresh IV each time).
        FieldCipher fieldCipher = FieldCipher.fromEnvironment(FieldCipher.from Environment); // key is currently unused — see FieldCipher TODO
        LedgerRepository repository = new LedgerRepository(database.getDataSource(), fieldCipher);
        TransferService transferService = new TransferService(database.getDataSource());

        // ACC-00000001 (500.00) and ACC-00000002 (100.00) are seeded by data.sql
        repository.createAccount("ACC-00000003", new BigDecimal("250.00"));
        repository.createAccountHolder(new AccountHolder(
                "ACC-00000001", "Jordan Rivera", "123-45-6789", Instant.now()));

        transferService.transfer("ACC-00000001", "ACC-00000002", new BigDecimal("50.00"));
        repository.recordTransactionLogEntry(
                "ACC-00000001", "ACC-00000002", new BigDecimal("50.00"), "demo transfer");

        repository.findAccountById("ACC-00000001")
                .map(Account::balance)
                .ifPresent(balance -> System.out.println("ACC-00000001 balance: " + balance));

        repository.findHolderByAccountId("ACC-00000001")
                .ifPresent(holder -> System.out.println(
                        "Holder on file: " + holder.displayName() + " (taxId: " + holder.taxId() + ")"));

        Path logPath = writeSampleLog();
        TransactionLogProcessor processor = new TransactionLogProcessor();
        List<TransactionRecord> records = processor.processLog(logPath);
        System.out.println("Processed " + records.size() + " transaction-log records.");

        Files.deleteIfExists(logPath);
    }

    private static Path writeSampleLog() throws IOException {
        Path logPath = Files.createTempFile("ledgercore-transaction-log", ".csv");
        List<String> lines = List.of(
                "ACC-00000001,ACC-00000002,50.00,demo transfer," + Instant.now(),
                "ACC-00000002,ACC-00000001,10.00,refund," + Instant.now(),
                "not,a,valid,line" // deliberately malformed — the pipeline filters this out
        );
        Files.write(logPath, lines, StandardCharsets.UTF_8);
        return logPath;
    }
}
