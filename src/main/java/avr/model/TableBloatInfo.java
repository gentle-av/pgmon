package avr.model;

public record TableBloatInfo(
    String schemaName,
    String tableName,
    long tableSizeBytes,
    long indexSizeBytes,
    long wasteBytes,
    double wastePercent,
    String suggestion) {
}
