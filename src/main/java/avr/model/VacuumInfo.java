package avr.model;

public record VacuumInfo(
    String schemaName,
    String tableName,
    long deadTuples,
    long liveTuples,
    double deadTupleRatio,
    String lastVacuum,
    String lastAutoVacuum,
    String lastAnalyze,
    long vacuumCount,
    long autovacuumCount) {
}
