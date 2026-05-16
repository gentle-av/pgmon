package avr.config;

import java.io.InputStream;
import java.util.List;

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

      long enabledCount = databases.stream().filter(DatabaseConfig::enabled).count();
      log.info("Конфигурация загружена. Найдено {} баз данных для мониторинга", enabledCount);
    } catch (Exception e) {
      log.error("Ошибка загрузки конфигурации: {}", e.getMessage());
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
