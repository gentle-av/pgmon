package avr.config;

public record WebConfig(
    boolean enabled,
    String staticPath,
    String title,
    int refreshIntervalSeconds,
    String theme) {
}
