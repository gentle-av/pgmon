package avr.model;

public record ReplicationStatus(
    String applicationName,
    String clientAddr,
    String state,
    long sentLsn,
    long writeLsn,
    long flushLsn,
    long replayLsn,
    double syncLagSeconds,
    String backendStart,
    boolean isSyncStandby) {
}
