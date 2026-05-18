package avr.model;

public record ActivePollingConfig(
    String serverId,
    String serverName,
    String credentialId,
    int priority,
    int pollingIntervalSeconds,
    int ashCollectionIntervalSeconds,
    Integer slowQueryThresholdMs,
    boolean collectQueries,
    boolean collectLocks,
    String jdbcUrl
) {}
