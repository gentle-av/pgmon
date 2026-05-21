package avr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "polling_configurations")
public class PollingConfiguration {
    @Id
    private String id;
    @Column(name = "server_id")
    private String serverId;
    @Column(name = "is_active")
    private boolean isActive = true;
    private Integer priority = 5;
    @Column(name = "schedule_type")
    private String scheduleType = "interval";
    @Column(name = "polling_interval_seconds")
    private Integer pollingIntervalSeconds = 30;
    @Column(name = "polling_interval_ms")
    private Integer pollingIntervalMs = 5000;
    @Column(name = "ash_collection_interval_ms")
    private Integer ashCollectionIntervalMs = 2000;
    @Column(name = "sessions_collection_interval_ms")
    private Integer sessionsCollectionIntervalMs = 1000;
    @Column(name = "ash_collection_interval_seconds")
    private Integer ashCollectionIntervalSeconds = 2;
    @Column(name = "cron_expression")
    private String cronExpression;
    private String timezone = "UTC";
    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "end_time")
    private LocalTime endTime;
    @Column(name = "active_days")
    private String activeDays = "1234567";
    @Column(name = "collect_queries")
    private boolean collectQueries = true;
    @Column(name = "collect_connections")
    private boolean collectConnections = true;
    @Column(name = "collect_locks")
    private boolean collectLocks = true;
    @Column(name = "collect_cache_hit_ratio")
    private boolean collectCacheHitRatio = true;
    @Column(name = "collect_unused_indexes")
    private boolean collectUnusedIndexes = false;
    @Column(name = "collect_vacuum_stats")
    private boolean collectVacuumStats = true;
    @Column(name = "collect_ash_data")
    private boolean collectAshData = true;
    @Column(name = "store_empty_snapshots")
    private Boolean storeEmptySnapshots = true;
    @Column(name = "slow_query_threshold_ms")
    private Integer slowQueryThresholdMs;
    @Column(name = "max_slow_queries")
    private Integer maxSlowQueries = 10;
    @Column(name = "dead_tuple_ratio_threshold_percent")
    private Double deadTupleRatioThresholdPercent = 10.0;
    @Column(name = "last_polling_start")
    private LocalDateTime lastPollingStart;
    @Column(name = "last_polling_end")
    private LocalDateTime lastPollingEnd;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }
    public Integer getPollingIntervalSeconds() { return pollingIntervalSeconds; }
    public void setPollingIntervalSeconds(Integer pollingIntervalSeconds) { this.pollingIntervalSeconds = pollingIntervalSeconds; }
    public Integer getPollingIntervalMs() { return pollingIntervalMs; }
    public void setPollingIntervalMs(Integer pollingIntervalMs) { this.pollingIntervalMs = pollingIntervalMs; }
    public Integer getAshCollectionIntervalMs() { return ashCollectionIntervalMs; }
    public void setAshCollectionIntervalMs(Integer ashCollectionIntervalMs) { this.ashCollectionIntervalMs = ashCollectionIntervalMs; }
    public Integer getSessionsCollectionIntervalMs() { return sessionsCollectionIntervalMs; }
    public void setSessionsCollectionIntervalMs(Integer sessionsCollectionIntervalMs) { this.sessionsCollectionIntervalMs = sessionsCollectionIntervalMs; }
    public Integer getAshCollectionIntervalSeconds() { return ashCollectionIntervalSeconds; }
    public void setAshCollectionIntervalSeconds(Integer ashCollectionIntervalSeconds) { this.ashCollectionIntervalSeconds = ashCollectionIntervalSeconds; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getActiveDays() { return activeDays; }
    public void setActiveDays(String activeDays) { this.activeDays = activeDays; }
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
    public Boolean getStoreEmptySnapshots() { return storeEmptySnapshots; }
    public void setStoreEmptySnapshots(Boolean storeEmptySnapshots) { this.storeEmptySnapshots = storeEmptySnapshots; }
    public Integer getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(Integer slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }
    public Integer getMaxSlowQueries() { return maxSlowQueries; }
    public void setMaxSlowQueries(Integer maxSlowQueries) { this.maxSlowQueries = maxSlowQueries; }
    public Double getDeadTupleRatioThresholdPercent() { return deadTupleRatioThresholdPercent; }
    public void setDeadTupleRatioThresholdPercent(Double deadTupleRatioThresholdPercent) { this.deadTupleRatioThresholdPercent = deadTupleRatioThresholdPercent; }
    public LocalDateTime getLastPollingStart() { return lastPollingStart; }
    public void setLastPollingStart(LocalDateTime lastPollingStart) { this.lastPollingStart = lastPollingStart; }
    public LocalDateTime getLastPollingEnd() { return lastPollingEnd; }
    public void setLastPollingEnd(LocalDateTime lastPollingEnd) { this.lastPollingEnd = lastPollingEnd; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
