package avr.model;

public record TableSizeInfo(
    String schemaName,
    String tableName,
    long tableSizeBytes,
    String tableSizeHuman,
    long indexesSizeBytes,
    String indexesSizeHuman,
    long totalSizeBytes,
    String totalSizeHuman) {
}
