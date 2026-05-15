package avr.config;

public record ConnectionThresholds(
    int maxConnections,
    int minAvailableConnections) {
}
