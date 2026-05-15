package avr.config;

public record AlertingSettings(
    boolean enabled,
    String webhookUrl,
    ConnectionThresholds connectionThresholds,
    int lockThresholdSeconds,
    int cacheHitRatioMinPercent) {
}
