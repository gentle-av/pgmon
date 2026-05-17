package avr.service;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class DatabaseInitializationService {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializationService.class);
    private final MonitoringConfig monitoringConfig;
    private final Map<String, Boolean> initialized = new ConcurrentHashMap<>();
    private String schemaSql;
    public DatabaseInitializationService(MonitoringConfig monitoringConfig) {
        this.monitoringConfig = monitoringConfig;
    }
    @PostConstruct
    public void loadSql() throws Exception {
        var resource = new ClassPathResource("db/migration/ash_schema.sql");
        schemaSql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        log.info("Загружен SQL скрипт из db/migration/ash_schema.sql");
    }
    @PostConstruct
    public void initAllDatabases() {
        for (ConfigRoot.DatabaseConfig db : monitoringConfig.getDatabaseConfigs()) {
            if (db.isEnabled()) {
                initDatabase(db);
            }
        }
    }
    private void initDatabase(ConfigRoot.DatabaseConfig db) {
        log.info("Инициализация БД: {} (SSL: {})", db.getName(), db.getSsl() != null && db.getSsl().isEnabled());
        try (Connection conn = createConnection(db);
             Statement stmt = conn.createStatement()) {
            stmt.execute(schemaSql);
            initialized.put(db.getName(), true);
            log.info("✅ БД {} инициализирована", db.getName());
        } catch (Exception e) {
            log.error("❌ Ошибка инициализации БД {}: {}", db.getName(), e.getMessage());
        }
    }
    private Connection createConnection(ConfigRoot.DatabaseConfig db) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            String url = String.format("jdbc:postgresql://%s:%d/%s", db.getHost(), db.getPort(), db.getDatabase());
            Properties props = new Properties();
            props.setProperty("user", db.getUsername());
            props.setProperty("password", db.getPassword());
            if (db.getSsl() != null && db.getSsl().isEnabled()) {
                props.setProperty("ssl", "true");
                props.setProperty("sslmode", "require");
            }
            return DriverManager.getConnection(url, props);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
    }
    public boolean isInitialized(String dbName) {
        return initialized.getOrDefault(dbName, false);
    }
}
