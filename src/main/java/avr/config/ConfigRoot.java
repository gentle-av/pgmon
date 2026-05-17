package avr.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfigRoot(
    ServerConfig server,
    WebConfig web,
    List<DatabaseConfig> databases,
    MonitoringSettings monitoring,
    AlertingSettings alerting
) {
    public ConfigRoot {
        if (server == null) {
            throw new IllegalArgumentException("server configuration is required");
        }
        if (databases == null || databases.isEmpty()) {
            throw new IllegalArgumentException("at least one database configuration is required");
        }
        if (monitoring == null) {
            throw new IllegalArgumentException("monitoring configuration is required");
        }
        if (alerting == null) {
            throw new IllegalArgumentException("alerting configuration is required");
        }
    }
}
