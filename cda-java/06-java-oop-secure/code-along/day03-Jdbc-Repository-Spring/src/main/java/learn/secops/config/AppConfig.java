package learn.secops.config;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Tells Spring's IoC container two things: where to find our
 * @Repository classes (component scan), and how to build the one
 * DataSource every repository in this app shares (the @Bean method
 * below). Spring calls dataSource() once, holds onto the result, and
 * hands it to any class -- like JdbcTemplate, or a repository -- that
 * asks for a DataSource in its constructor.
 */
@Configuration
@ComponentScan(basePackages = "learn.secops.data")
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{"localhost"});
        dataSource.setPortNumbers(new int[]{5432});
        dataSource.setDatabaseName("secops");
        dataSource.setUser("secops_app");
        dataSource.setPassword(System.getenv("SECOPS_DB_PASSWORD"));
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
