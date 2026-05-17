package avr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
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
        // Правильный путь к файлу
        var resource = new ClassPathResource("db/migration/ash_schema.sql");
        schemaSql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        log.info("Загружен SQL скрипт из db/migration/ash_schema.sql");
    }

    @PostConstruct
    public void initAllDatabases() {
        for (DatabaseConfig db : monitoringConfig.getDatabases()) {
            if (db.enabled()) {
                initDatabase(db);
            }
        }
    }

    private void initDatabase(DatabaseConfig db) {
        log.info("Инициализация БД: {}", db.name());

        try (Connection conn = DriverManager.getConnection(
                db.getJdbcUrl(), db.username(), db.password());
             Statement stmt = conn.createStatement()) {

            stmt.execute(schemaSql);
            initialized.put(db.name(), true);
            log.info("✅ БД {} инициализирована", db.name());

        } catch (Exception e) {
            log.error("❌ Ошибка инициализации БД {}: {}", db.name(), e.getMessage());
        }
    }

    public boolean isInitialized(String dbName) {
        return initialized.getOrDefault(dbName, false);
    }
}
