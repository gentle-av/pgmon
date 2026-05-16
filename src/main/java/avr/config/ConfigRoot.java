package avr.config;

import java.util.List;

record ConfigRoot(
    ServerConfig server,
    WebConfig web,
    List<DatabaseConfig> databases,
    MonitoringSettings monitoring,
    AlertingSettings alerting) {
}
