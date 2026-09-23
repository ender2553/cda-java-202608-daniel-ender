package com.cyberdev.dndrpg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Reads DB connection settings from environment variables and builds a
 * Spring DataSource from them. DriverManagerDataSource opens a new
 * physical connection on every call -- no pooling -- which keeps this
 * teaching exercise dependency-free. A real deployment would use a
 * pooled DataSource (HikariCP) instead; JdbcTemplate itself doesn't
 * care which kind of DataSource it's handed.
 */
@Configuration(proxyBeanMethods = false)
@Profile("jdbc")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String url = System.getenv().getOrDefault("DNDRPG_DB_URL", "jdbc:postgresql://localhost:5432/dndrpg");
        String user = System.getenv().getOrDefault("DNDRPG_DB_USER", "dndrpg");
        String password = System.getenv().getOrDefault("DNDRPG_DB_PASSWORD", "");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
