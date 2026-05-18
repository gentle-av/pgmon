package avr.dto;

import java.time.LocalDateTime;

public class ActiveSessionsRequest {
  private String serverId;
  private LocalDateTime from;
  private LocalDateTime to;

  public String getServerId() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId = serverId;
  }

  public LocalDateTime getFrom() {
    return from;
  }

  public void setFrom(LocalDateTime from) {
    this.from = from;
  }

  public LocalDateTime getTo() {
    return to;
  }

  public void setTo(LocalDateTime to) {
    this.to = to;
  }
}
