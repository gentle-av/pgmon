package avr.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import avr.config.DatabaseConfig;
import avr.config.MonitoringConfig;
import avr.config.MonitoringSettings;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

  private final MonitoringConfig monitoringConfig;

  public ConfigController(MonitoringConfig monitoringConfig) {
    this.monitoringConfig = monitoringConfig;
  }

  @GetMapping("/databases")
  public List<DatabaseConfig> getDatabases() {
    return monitoringConfig.getDatabases();
  }

  @GetMapping("/server")
  public Map<String, Integer> getServerPort() {
    return Map.of("port", monitoringConfig.getServer().port());
  }

  @GetMapping("/monitoring")
  public MonitoringSettings getMonitoringSettings() {
    return monitoringConfig.getMonitoring();
  }
}
