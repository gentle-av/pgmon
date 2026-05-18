package avr.dto;

public class UpdatePollingRequest {
    private int pollingIntervalSeconds;
    private Integer slowQueryThresholdMs;
    private Integer maxSlowQueries;
    private int priority;
    private boolean collectQueries;
    private boolean collectConnections;
    private boolean collectLocks;
    private boolean collectCacheHitRatio;
    private boolean collectUnusedIndexes;
    private boolean collectVacuumStats;
    private boolean collectAshData;

    public int getPollingIntervalSeconds() { return pollingIntervalSeconds; }
    public void setPollingIntervalSeconds(int pollingIntervalSeconds) { this.pollingIntervalSeconds = pollingIntervalSeconds; }
    public Integer getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(Integer slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }
    public Integer getMaxSlowQueries() { return maxSlowQueries; }
    public void setMaxSlowQueries(Integer maxSlowQueries) { this.maxSlowQueries = maxSlowQueries; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public boolean isCollectQueries() { return collectQueries; }
    public void setCollectQueries(boolean collectQueries) { this.collectQueries = collectQueries; }
    public boolean isCollectConnections() { return collectConnections; }
    public void setCollectConnections(boolean collectConnections) { this.collectConnections = collectConnections; }
    public boolean isCollectLocks() { return collectLocks; }
    public void setCollectLocks(boolean collectLocks) { this.collectLocks = collectLocks; }
    public boolean isCollectCacheHitRatio() { return collectCacheHitRatio; }
    public void setCollectCacheHitRatio(boolean collectCacheHitRatio) { this.collectCacheHitRatio = collectCacheHitRatio; }
    public boolean isCollectUnusedIndexes() { return collectUnusedIndexes; }
    public void setCollectUnusedIndexes(boolean collectUnusedIndexes) { this.collectUnusedIndexes = collectUnusedIndexes; }
    public boolean isCollectVacuumStats() { return collectVacuumStats; }
    public void setCollectVacuumStats(boolean collectVacuumStats) { this.collectVacuumStats = collectVacuumStats; }
    public boolean isCollectAshData() { return collectAshData; }
    public void setCollectAshData(boolean collectAshData) { this.collectAshData = collectAshData; }
}
