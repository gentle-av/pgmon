package avr.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class MonitoringConfig {

    private static final Logger log = LoggerFactory.getLogger(MonitoringConfig.class);
    private final Json5Parser json5Parser;
    private ServerConfig server;
    private List<DatabaseConfig> databases;
    private MonitoringSettings monitoring;
    private AlertingSettings alerting;
    private WebConfig web;

    public MonitoringConfig(Json5Parser json5Parser) {
        this.json5Parser = json5Parser;
    }

    @PostConstruct
    public void loadConfig() {
        try {
            ClassPathResource resource = new ClassPathResource("monitoring-config.json5");
            InputStream inputStream = resource.getInputStream();
            ConfigRoot configRoot = json5Parser.parse(inputStream, ConfigRoot.class);
            this.server = configRoot.server();
            this.databases = configRoot.databases();
            this.monitoring = configRoot.monitoring();
            this.alerting = configRoot.alerting();
            this.web = configRoot.web();
            List<DatabaseConfig> enabledDatabases = new ArrayList<>();
            for (DatabaseConfig db : databases) {
                if (db.enabled()) {
                    enabledDatabases.add(db);
                    log.info("Добавлена БД для мониторинга: {} -> {}", db.name(), db.getJdbcUrl());
                }
            }
            this.databases = enabledDatabases;
            log.info("Конфигурация загружена. Баз данных: {}", databases.size());
        } catch (Exception e) {
            log.error("Ошибка загрузки конфигурации: {}", e.getMessage());
            throw new RuntimeException("Не удалось загрузить monitoring-config.json5", e);
        }
    }

    public ServerConfig getServer() {
        return server;
    }

    public List<DatabaseConfig> getDatabases() {
        return databases;
    }

    public MonitoringSettings getMonitoring() {
        return monitoring;
    }

    public AlertingSettings getAlerting() {
        return alerting;
    }

    public WebConfig getWeb() {
        return web;
    }
}
