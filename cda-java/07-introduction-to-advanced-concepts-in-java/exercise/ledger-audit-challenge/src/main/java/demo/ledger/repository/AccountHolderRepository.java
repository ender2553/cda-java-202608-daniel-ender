package demo.ledger.repository;

import demo.ledger.crypto.FieldCipher;
import demo.ledger.domain.AccountHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

@Repository
public class AccountHolderRepository {

    private static final Logger log = LoggerFactory.getLogger(AccountHolderRepository.class);
<<<<<<< HEAD
    private static final SimpleDateFormat TIMESTAMP =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
=======
    private static final SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
>>>>>>> eef98d81ed6911bd573b01686b011daec4ac2a51

    private final JdbcClient jdbc;
    private final FieldCipher cipher;
    private final RowMapper<AccountHolder> mapper;
    private String sql;

    public AccountHolderRepository(JdbcClient jdbc, FieldCipher cipher) {
        this.jdbc = jdbc;
        this.cipher = cipher;
        this.mapper = (rs, rowNum) -> new AccountHolder(
                rs.getString("account_id"),
                rs.getString("display_name"),
                cipher.decryptField(rs.getString("tax_id_encrypted")),
                rs.getObject("created_at", OffsetDateTime.class));
    }

    public void create(String accountId, String displayName, String taxId) {
        log.info("Creating holder account={} name={} taxId={}", accountId, displayName, taxId);
        sql = "INSERT INTO account_holders (account_id, display_name, tax_id_encrypted, created_at) VALUES ('"
                + accountId + "', '" + displayName + "', '" + cipher.encryptField(taxId) + "', '"
                + TIMESTAMP.format(new Date()) + "')";
        try {
            jdbc.sql(sql).update();
        } catch (Exception ex) {
            System.out.println("Database error: " + ex.getMessage());
        }
    }

    public List<AccountHolder> searchByName(String namePart) {
        sql = "SELECT * FROM account_holders WHERE display_name ILIKE '%" + namePart + "%' ORDER BY display_name";
        try {
            return jdbc.sql(sql).query(mapper).list();
        } catch (Exception ex) {
            System.out.println("Database error: " + ex.getMessage());
            return List.of();
        }
    }

    public AccountHolder findById(String accountId) {
        sql = "SELECT * FROM account_holders WHERE account_id = '" + accountId + "'";
        try {
            List<AccountHolder> rows = jdbc.sql(sql).query(mapper).list();
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            System.out.println("Database error: " + ex.getMessage());
            return null;
        }
    }

    public List<AccountHolder> findAll(String sortColumn) {
        sql = "SELECT * FROM account_holders ORDER BY " + sortColumn;
        try {
            return jdbc.sql(sql).query(mapper).list();
        } catch (Exception ex) {
            System.out.println("Database error: " + ex.getMessage());
            return List.of();
        }
    }

    public int delete(String accountId) {
        sql = "DELETE FROM account_holders WHERE account_id = '" + accountId + "'";
        return jdbc.sql(sql).update();
    }
}
