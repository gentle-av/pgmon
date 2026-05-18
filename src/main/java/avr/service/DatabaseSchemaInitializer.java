package avr.service;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import avr.config.StorageDataSourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

@Service
public class DatabaseSchemaInitializer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);
    private final DataSource dataSource;
    private final ConfigRoot.StorageConfig storageConfig;

    private final List<String> migrationFiles = Arrays.asList(
        "db/migration/01_credentials_table.sql",
        "db/migration/02_servers_table.sql",
        "db/migration/03_polling_configurations_table.sql",
        "db/migration/04_polling_history_table.sql",
        "db/migration/05_ash_history_table.sql",
        "db/migration/06_functions_triggers.sql",
        "db/migration/07_initial_data.sql"
    );

    public DatabaseSchemaInitializer(StorageDataSourceConfig storageDataSourceConfig, MonitoringConfig monitoringConfig) {
        this.dataSource = storageDataSourceConfig.storageDataSource();
        this.storageConfig = monitoringConfig.getStorageConfig();
    }

    @PostConstruct
    public void initialize() {
        String schema = storageConfig.getSchema() != null ? storageConfig.getSchema() : "public";
        for (String migrationFile : migrationFiles) {
            try {
                log.info("Выполнение миграции: {}", migrationFile);
                var resource = new ClassPathResource(migrationFile);
                String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                sql = sql.replaceAll("public\\.", schema + ".");
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    for (String statement : sql.split(";")) {
                        String trimmed = statement.trim();
                        if (!trimmed.isEmpty()) {
                            stmt.execute(trimmed);
                        }
                    }
                }
                log.info("✅ Миграция {} выполнена", migrationFile);
            } catch (Exception e) {
                log.error("❌ Ошибка выполнения миграции {}: {}", migrationFile, e.getMessage());
            }
        }
    }
}
