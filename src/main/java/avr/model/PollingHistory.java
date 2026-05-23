package avr.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "polling_history")
public class PollingHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "server_id")
  private String serverId;
  @Column(name = "polling_config_id")
  private String pollingConfigId;
  @Column(name = "polling_start")
  private LocalDateTime pollingStart;
  @Column(name = "polling_end")
  private LocalDateTime pollingEnd;
  @Column(name = "duration_ms")
  private Long durationMs;
  private String status;
  @Column(name = "error_message")
  private String errorMessage;
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getServerId() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId = serverId;
  }

  public String getPollingConfigId() {
    return pollingConfigId;
  }

  public void setPollingConfigId(String pollingConfigId) {
    this.pollingConfigId = pollingConfigId;
  }

  public LocalDateTime getPollingStart() {
    return pollingStart;
  }

  public void setPollingStart(LocalDateTime pollingStart) {
    this.pollingStart = pollingStart;
  }

  public LocalDateTime getPollingEnd() {
    return pollingEnd;
  }

  public void setPollingEnd(LocalDateTime pollingEnd) {
    this.pollingEnd = pollingEnd;
  }

  public Long getDurationMs() {
    return durationMs;
  }

  public void setDurationMs(Long durationMs) {
    this.durationMs = durationMs;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
