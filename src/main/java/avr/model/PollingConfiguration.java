package avr.model;

import jakarta.persistence.*;
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
  @Column(name = "polling_interval_ms")
  private Integer pollingIntervalMs = 30000;
  @Column(name = "ash_collection_interval_ms")
  private Integer ashCollectionIntervalMs = 2000;
  @Column(name = "sessions_collection_interval_ms")
  private Integer sessionsCollectionIntervalMs = 1000;
  @Column(name = "collect_ash_data")
  private boolean collectAshData = true;
  @Column(name = "store_empty_snapshots")
  private Boolean storeEmptySnapshots = true;
  @Column(name = "start_time")
  private LocalTime startTime;
  @Column(name = "end_time")
  private LocalTime endTime;
  @Column(name = "active_days")
  private String activeDays = "1234567";
  @Column(name = "created_at")
  private LocalDateTime createdAt;
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getServerId() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId = serverId;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public Integer getPollingIntervalMs() {
    return pollingIntervalMs;
  }

  public void setPollingIntervalMs(Integer pollingIntervalMs) {
    this.pollingIntervalMs = pollingIntervalMs;
  }

  public Integer getAshCollectionIntervalMs() {
    return ashCollectionIntervalMs;
  }

  public void setAshCollectionIntervalMs(Integer ashCollectionIntervalMs) {
    this.ashCollectionIntervalMs = ashCollectionIntervalMs;
  }

  public Integer getSessionsCollectionIntervalMs() {
    return sessionsCollectionIntervalMs;
  }

  public void setSessionsCollectionIntervalMs(Integer sessionsCollectionIntervalMs) {
    this.sessionsCollectionIntervalMs = sessionsCollectionIntervalMs;
  }

  public boolean isCollectAshData() {
    return collectAshData;
  }

  public void setCollectAshData(boolean collectAshData) {
    this.collectAshData = collectAshData;
  }

  public Boolean getStoreEmptySnapshots() {
    return storeEmptySnapshots;
  }

  public void setStoreEmptySnapshots(Boolean storeEmptySnapshots) {
    this.storeEmptySnapshots = storeEmptySnapshots;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public String getActiveDays() {
    return activeDays;
  }

  public void setActiveDays(String activeDays) {
    this.activeDays = activeDays;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

}
