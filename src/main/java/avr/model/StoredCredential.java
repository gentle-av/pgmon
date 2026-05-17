package avr.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "stored_database_credentials")
public class StoredCredential {
    @Id
    private String id;
    private String name;
    private String host;
    private Integer port;
    @Column(name = "database_name")
    private String databaseName;
    private String username;
    @Column(name = "encrypted_password")
    private String encryptedPassword;
    @Column(name = "ssl_mode")
    private String sslMode;
    private Boolean enabled;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
    public String getSslMode() { return sslMode; }
    public void setSslMode(String sslMode) { this.sslMode = sslMode; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String buildJdbcUrl() {
        String baseUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, databaseName);
        if (sslMode != null && !sslMode.isEmpty() && !"disable".equals(sslMode)) {
            return baseUrl + "?ssl=true&sslmode=" + sslMode;
        }
        return baseUrl;
    }
}
