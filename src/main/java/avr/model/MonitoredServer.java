package avr.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "monitored_servers")
public class MonitoredServer {
    @Id
    private String id;

    @Column(name = "credential_id")
    private String credentialId;

    @Column(name = "server_name")
    private String serverName;

    @Column(name = "display_name")
    private String displayName;

    private String environment;

    private String description;

    @Column(name = "connection_timeout_ms")
    private int connectionTimeoutMs = 5000;

    private boolean enabled = true;

    private String status = "unknown";

    @Column(name = "last_checked")
    private LocalDateTime lastChecked;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getCredentialId() { return credentialId; }

    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getServerName() { return serverName; }

    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEnvironment() { return environment; }

    public void setEnvironment(String environment) { this.environment = environment; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public int getConnectionTimeoutMs() { return connectionTimeoutMs; }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) { this.connectionTimeoutMs = connectionTimeoutMs; }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastChecked() { return lastChecked; }

    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }

    public String getLastError() { return lastError; }

    public void setLastError(String lastError) { this.lastError = lastError; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
