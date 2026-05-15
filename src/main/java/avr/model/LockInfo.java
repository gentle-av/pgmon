package avr.model;

public record LockInfo(
    Integer blockedPid,
    String blockedUsername,
    String blockedQuery,
    Integer blockingPid,
    String blockingUsername,
    String blockingQuery,
    String lockType,
    String relation,
    Integer waitSeconds) {
}
