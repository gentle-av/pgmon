package avr.dto;

public class UpdatePollingRequest {
  private Integer pollingIntervalMs;
  private Integer ashCollectionIntervalMs;
  private Integer sessionsCollectionIntervalMs;
  private Boolean storeEmptySnapshots;
  private boolean collectAshData;

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

  public Boolean getStoreEmptySnapshots() {
    return storeEmptySnapshots;
  }

  public void setStoreEmptySnapshots(Boolean storeEmptySnapshots) {
    this.storeEmptySnapshots = storeEmptySnapshots;
  }

  public boolean isCollectAshData() {
    return collectAshData;
  }

  public void setCollectAshData(boolean collectAshData) {
    this.collectAshData = collectAshData;
  }
}
