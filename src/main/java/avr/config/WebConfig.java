package avr.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebConfig(
    boolean enabled,
    String staticPath,
    String title,
    int refreshIntervalSeconds,
    String theme
) {
    public WebConfig() {
        this(true, "/web", "PostgreSQL Monitor", 10, "dark");
    }
}
