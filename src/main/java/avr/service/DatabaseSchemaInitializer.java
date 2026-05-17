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
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;

@Service
public class DatabaseSchemaInitializer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);
    private final DataSource dataSource;
    private final ConfigRoot.StorageConfig storageConfig;
    public DatabaseSchemaInitializer(StorageDataSourceConfig storageDataSourceConfig, MonitoringConfig monitoringConfig) {
        this.dataSource = storageDataSourceConfig.storageDataSource();
        this.storageConfig = monitoringConfig.getStorageConfig();
    }
    @PostConstruct
    public void initialize() {
        try {
            if (!tableExists("stored_database_credentials")) {
                createCredentialsTable();
                log.info("✅ Таблица stored_database_credentials создана в {}/{}", storageConfig.getDatabase(), storageConfig.getSchema());
            } else {
                log.info("✅ Таблица stored_database_credentials уже существует");
            }
            if (!tableExists("pash_ash_history")) {
                createAshTable();
                log.info("✅ Таблица pash_ash_history создана в {}/{}", storageConfig.getDatabase(), storageConfig.getSchema());
            } else {
                log.info("✅ Таблица pash_ash_history уже существует");
            }
        } catch (Exception e) {
            log.error("Ошибка инициализации схемы БД: {}", e.getMessage(), e);
        }
    }
    private boolean tableExists(String tableName) throws Exception {
        String sql = String.format("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '%s' AND table_schema = '%s'", tableName, storageConfig.getSchema());
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    private void createCredentialsTable() throws Exception {
        var resource = new ClassPathResource("db/migration/add_credential_table.sql");
        String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        sql = sql.replace("public.", storageConfig.getSchema() + ".");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }
    private void createAshTable() throws Exception {
        var resource = new ClassPathResource("db/migration/ash_schema.sql");
        String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        sql = sql.replace("public.", storageConfig.getSchema() + ".");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
