package avr.config;

import java.util.List;

record ConfigRoot(
    ServerConfig server,
    List<DatabaseConfig> databases,
    MonitoringSettings monitoring,
    AlertingSettings alerting) {
}
