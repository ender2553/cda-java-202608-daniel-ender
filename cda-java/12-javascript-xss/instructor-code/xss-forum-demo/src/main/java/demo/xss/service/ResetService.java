package demo.xss.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/** Resets the demo — re-runs schema.sql to drop, recreate and re-seed every table. */
@Service
public class ResetService {

    private final DataSource dataSource;

    public ResetService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional
    public void resetDemo() {
        // Runs on this @Transactional method's connection, so the drop, create and seed commit or roll back together.
        new ResourceDatabasePopulator(new ClassPathResource("db/schema.sql")).execute(dataSource);
    }
}
