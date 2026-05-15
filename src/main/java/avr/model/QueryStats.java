package avr.model;

public record QueryStats(
    Long queryId,
    String query,
    String normalizedQuery,
    String databaseName,
    String username,
    long calls,
    double totalTimeMs,
    double meanTimeMs,
    double maxTimeMs,
    double minTimeMs,
    long rowsReturned,
    long sharedBlksHit,
    long sharedBlksRead,
    long tempBlksRead,
    long tempBlksWritten) {
}
