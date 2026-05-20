package avr.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import avr.config.ConfigRoot;
import avr.config.MonitoringConfig;
import jakarta.annotation.PostConstruct;

@Service
public class DatabaseInitializationService {
  private static final Logger log = LoggerFactory.getLogger(DatabaseInitializationService.class);
  private final MonitoringConfig monitoringConfig;
  private final Map<String, Boolean> initialized = new ConcurrentHashMap<>();

  public DatabaseInitializationService(MonitoringConfig monitoringConfig) {
    this.monitoringConfig = monitoringConfig;
  }

  @PostConstruct
  public void initAllDatabases() {
    for (ConfigRoot.DatabaseConfig db : monitoringConfig.getDatabaseConfigs()) {
      if (db.isEnabled()) {
        initialized.put(db.getName(), true);
        log.info("БД {} доступна для мониторинга", db.getName());
      }
    }
  }

  public boolean isInitialized(String dbName) {
    return initialized.getOrDefault(dbName, false);
  }
}
