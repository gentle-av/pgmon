package avr.model;

public record DatabaseStats(
    String databaseName,
    long sizeBytes,
    String sizeHuman,
    int activeConnections,
    int idleConnections,
    int totalConnections,
    double cacheHitRatio,
    double transactionCommitRatio,
    long tuplesInserted,
    long tuplesUpdated,
    long tuplesDeleted,
    long tuplesFetched,
    long deadTuples,
    String lastVacuum,
    String lastAutoVacuum,
    String lastAnalyze,
    double bloatPercent,
    int runningQueries,
    int waitingQueries,
    double avgQueryDurationMs,
    long tempFiles,
    long tempBytes) {
}
