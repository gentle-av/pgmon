package avr.config;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

@Component
public class MonitoringConfig {

  private static final Logger log = LoggerFactory.getLogger(MonitoringConfig.class);

  private ServerConfig server;
  private List<DatabaseConfig> databases;
  private MonitoringSettings monitoring;
  private AlertingSettings alerting;
  private WebConfig web;

  @PostConstruct
  public void loadConfig() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      ClassPathResource resource = new ClassPathResource("monitoring-config.json");
      InputStream inputStream = resource.getInputStream();
      ConfigRoot configRoot = mapper.readValue(inputStream, ConfigRoot.class);
      this.server = configRoot.server();
      this.databases = configRoot.databases();
      this.monitoring = configRoot.monitoring();
      this.alerting = configRoot.alerting();
      this.web = configRoot.web();
      List<DatabaseConfig> enabledDatabases = new ArrayList<>();
      for (DatabaseConfig db : databases) {
        if (db.enabled()) {
          enabledDatabases.add(db);
          log.info("✅ Добавлена БД для мониторинга: {} -> {}", db.name(), db.getJdbcUrl());
        } else {
          log.info("⏭️ БД {} отключена в конфигурации", db.name());
        }
      }
      this.databases = enabledDatabases;
      log.info("========================================");
      log.info("📋 Конфигурация загружена:");
      log.info("   Сервер порт: {}", server.port());
      log.info("   Web интерфейс: {} (theme: {})", web.enabled() ? "включен" : "отключен", web.theme());
      log.info("   Баз данных для мониторинга: {}", databases.size());
      log.info("   Интервал мониторинга: {} сек", monitoring.defaultIntervalSeconds());
      log.info("   Slow query threshold: {} ms", monitoring.slowQueryThresholdMs());
      log.info("   Alerting: {}", alerting.enabled() ? "включен" : "отключен");
      log.info("========================================");
    } catch (Exception e) {
      log.error("❌ Ошибка загрузки конфигурации: {}", e.getMessage());
      log.error("   Убедитесь, что файл monitoring-config.json существует в classpath");
      throw new RuntimeException("Не удалось загрузить monitoring-config.json", e);
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
