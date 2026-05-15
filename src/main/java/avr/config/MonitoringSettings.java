package avr.config;

public record MonitoringSettings(
    int defaultIntervalSeconds,
    boolean collectQueries,
    boolean collectConnections,
    boolean collectLocks,
    boolean collectCacheHitRatio,
    boolean collectTableSizes,
    int slowQueryThresholdMs,
    int maxSlowQueries) {
}
