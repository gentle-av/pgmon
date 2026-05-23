package avr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;

@Configuration
public class MonitoringConfig {
  private static final Logger log = LoggerFactory.getLogger(MonitoringConfig.class);
  private ConfigRoot configRoot;
  private final Json5Parser json5Parser;

  public MonitoringConfig(Json5Parser json5Parser) {
    this.json5Parser = json5Parser;
  }

  @PostConstruct
  public void loadConfig() throws Exception {
    File configFile = new File("monitoring-config.json5");
    if (!configFile.exists()) {
      configFile = new ClassPathResource("monitoring-config.json5").getFile();
    }
    try (FileInputStream fis = new FileInputStream(configFile)) {
      this.configRoot = json5Parser.parse(fis, ConfigRoot.class);
    }
    if (configRoot.getStorage() != null && configRoot.getStorage().getPassword() != null
        && configRoot.getStorage().getPassword().startsWith("${")) {
      String envVar = configRoot.getStorage().getPassword().substring(2,
          configRoot.getStorage().getPassword().length() - 1);
      configRoot.getStorage().setPassword(System.getenv(envVar));
    }
    log.info("Конфигурация загружена. Storage БД: {}/{}",
        configRoot.getStorage().getHost(),
        configRoot.getStorage().getDatabase());
  }

  public ConfigRoot.ServerConfig getServerConfig() {
    return configRoot.getServer();
  }

  public ConfigRoot.MonitoringSettings getMonitoringSettings() {
    return configRoot.getMonitoring();
  }

  public ConfigRoot.AlertingSettings getAlertingSettings() {
    return configRoot.getAlerting();
  }

  public ConfigRoot.StorageConfig getStorageConfig() {
    return configRoot.getStorage();
  }
}
