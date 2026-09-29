package com.rti.ledgercore.repository;

import com.rti.ledgercore.crypto.FieldCipher;
import com.rti.ledgercore.domain.Account;
import com.rti.ledgercore.domain.AccountHolder;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

/**
 * LedgerCore's data-access layer.
 *
 * <p><b>This is the insecure starting point for the lab.</b> Every query below
 * is built by concatenating input directly into SQL text, nothing validates
 * an input before it reaches the database, and a caught {@link SQLException}
 * is surfaced to the caller with its raw message intact.
 */
public final class LedgerRepository {

    private final DataSource dataSource;
    private final FieldCipher fieldCipher;

    public LedgerRepository(DataSource dataSource, FieldCipher fieldCipher) {
        this.dataSource = dataSource;
        this.fieldCipher = fieldCipher;
    }

    // COMPLETE (lab): parameterize, validate at the boundary, handle exceptions securely.
    public void createAccount(String accountId, BigDecimal initialBalance) {
        if (accountId == null || accountId.isBlank()){
            throw new IllegalArgumentException("account Id is required");
        }
        if (initialBalance == null || initialBalance.signum() < 0){
            throw new IllegalArgumentException("initial balance must be greater than 0");
        }

        String sql = "INSERT INTO accounts (account_id, balance) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountId);
            statement.setBigDecimal(2, initialBalance);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex); // INSECURE: leaks raw driver/SQL detail to the caller
        }
    }

    public Optional<Account> findAccountById(String accountId) {
        if (accountId == null || accountId.isBlank()){
            throw new IllegalArgumentException("account Id is required");
        }

        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, accountId);
             try (ResultSet resultSet = statement.executeQuery()){
                 if (resultSet.next()) {
                     return Optional.of(new Account(resultSet.getString("account_id"), resultSet.getBigDecimal("balance")));
                 }
             }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public void createAccountHolder(AccountHolder holder) {
        String storedTaxId = fieldCipher.encryptField(holder.taxId()); // passthrough baseline — see FieldCipher
        String sql = "INSERT INTO account_holders (account_id, display_name, tax_id_encrypted, created_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, holder.accountId());
            statement.setString(2, holder.displayName());
            statement.setString(3, storedTaxId);
            statement.setTimestamp(4, Timestamp.from(holder.createdAt()));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public Optional<AccountHolder> findHolderByAccountId(String accountId) {
        String sql = "SELECT * FROM account_holders WHERE account_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){
             statement.setString(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String storedTaxId = resultSet.getString("tax_id_encrypted");
                    String taxId = fieldCipher.decryptField(storedTaxId);
                    return Optional.of(new AccountHolder(
                            resultSet.getString("account_id"),
                            resultSet.getString("display_name"),
                            taxId,
                            resultSet.getTimestamp("created_at").toInstant()));
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public void recordTransactionLogEntry(String fromAccountId, String toAccountId, BigDecimal amount, String memo) {
        String sql = "INSERT INTO transactions (from_account, to_account, amount, memo) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fromAccountId);
            statement.setString(2, toAccountId);
            statement.setBigDecimal(3, amount);
            statement.setString(4, memo);

            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
