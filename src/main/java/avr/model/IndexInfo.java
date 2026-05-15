package avr.model;

public record IndexInfo(
    String schemaName,
    String tableName,
    String indexName,
    long indexSizeBytes,
    String indexSizeHuman,
    long idxScan,
    long idxTupRead,
    long idxTupFetch,
    double usageRatio,
    boolean isPrimaryKey,
    boolean isUnique,
    String indexDef) {
}
