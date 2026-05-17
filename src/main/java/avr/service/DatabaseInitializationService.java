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
        for (DatabaseConfig db : monitoringConfig.getDatabases()) {
            if (db.enabled()) {
                initDatabase(db);
            }
        }
    }

    private void initDatabase(DatabaseConfig db) {
        log.info("Инициализация БД: {} (SSL: {})", db.name(), db.ssl().enabled());
        try (Connection conn = createConnection(db);
             Statement stmt = conn.createStatement()) {
            stmt.execute(schemaSql);
            initialized.put(db.name(), true);
            log.info("✅ БД {} инициализирована", db.name());
        } catch (Exception e) {
            log.error("❌ Ошибка инициализации БД {}: {}", db.name(), e.getMessage());
        }
    }

    private Connection createConnection(DatabaseConfig db) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            Properties props = new Properties();
            props.setProperty("user", db.username());
            props.setProperty("password", db.password());
            if (db.ssl().enabled()) {
                props.setProperty("ssl", "true");
                props.setProperty("sslmode", db.ssl().mode());
                if (db.ssl().sslCert() != null && !db.ssl().sslCert().isEmpty()) {
                    String certPath = getResourcePath(db.ssl().sslCert());
                    props.setProperty("sslcert", certPath);
                }
                if (db.ssl().sslKey() != null && !db.ssl().sslKey().isEmpty()) {
                    String keyPath = getResourcePath(db.ssl().sslKey());
                    props.setProperty("sslkey", keyPath);
                }
                if (db.ssl().sslRootCert() != null && !db.ssl().sslRootCert().isEmpty()) {
                    String rootCertPath = getResourcePath(db.ssl().sslRootCert());
                    props.setProperty("sslrootcert", rootCertPath);
                }
                log.debug("SSL подключение к БД {} с режимом {}", db.name(), db.ssl().mode());
            }
            return DriverManager.getConnection(db.getJdbcUrl(), props);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
    }

    private String getResourcePath(String resourcePath) {
        try {
            if (resourcePath.startsWith("classpath:")) {
                String path = resourcePath.substring("classpath:".length());
                var resource = new ClassPathResource(path);
                return resource.getFile().getAbsolutePath();
            }
            return resourcePath;
        } catch (Exception e) {
            log.warn("Не удалось загрузить ресурс: {}", resourcePath);
            return resourcePath;
        }
    }

    public boolean isInitialized(String dbName) {
        return initialized.getOrDefault(dbName, false);
    }
}
