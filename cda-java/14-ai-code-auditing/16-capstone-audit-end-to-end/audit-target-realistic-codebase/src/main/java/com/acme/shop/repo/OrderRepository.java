package com.acme.shop.repo;

import com.acme.shop.model.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Order> MAPPER = (rs, n) -> new Order(
            rs.getLong("id"),
            rs.getString("customer"),
            rs.getString("item"),
            rs.getInt("amount_cents"));

    public List<Order> searchByItem(String item, String customer) {
        String sql = """
        SELECT id, customer, item, amount_cents
        FROM orders
        WHERE customer = ?
        AND item LIKE ?
        """;

        return jdbc.query(
                sql,
                MAPPER,
                customer,
                "%" + item + "%"
        );
    }

    public Order findById(long id) {
        return jdbc.queryForObject(
                "SELECT id, customer, item, amount_cents FROM orders WHERE id = ?",
                MAPPER, id);
    }

    public List<Order> findByCustomer(String customer) {
        return jdbc.query(
                "SELECT id, customer, item, amount_cents FROM orders WHERE customer = ?",
                MAPPER, customer);
    }

    public java.util.Optional<Order> findByIdForCustomer(
            long id, String customer) {

        return jdbc.query(
                """
                SELECT id, customer, item, amount_cents
                FROM orders
                WHERE id = ? AND customer = ?
                """,
                MAPPER,
                id,
                customer
        ).stream().findFirst();
    }

}
