package avr.model;

public record ConnectionInfo(
    int pid,
    String username,
    String applicationName,
    String clientAddr,
    String backendStart,
    String state,
    String query,
    String waitEventType,
    String waitEvent,
    long queryDurationSeconds,
    boolean isIdleInTransaction) {
}
