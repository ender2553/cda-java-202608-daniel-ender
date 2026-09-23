package com.cybersoft.vehicle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/** Demonstrates externalized configuration. Do not hard-code database secrets. */
@Configuration
@Profile("jdbc")
public class DatabaseConfig {
    @Bean
    public DataSource dataSource(Environment environment) {
        var dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(environment.getRequiredProperty("VEHICLE_DB_URL"));
        dataSource.setUsername(environment.getRequiredProperty("VEHICLE_DB_USER"));
        dataSource.setPassword(environment.getRequiredProperty("VEHICLE_DB_PASSWORD"));
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
