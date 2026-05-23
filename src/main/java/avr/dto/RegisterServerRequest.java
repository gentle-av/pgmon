package avr.dto;

public class RegisterServerRequest {
  private String credentialName;
  private String serverName;
  private String displayName;
  private String environment;
  private String description;
  private String host;
  private Integer port;
  private String databaseName;
  private String username;
  private String password;
  private String sslMode;
  private int connectionTimeoutMs = 5000;
  private boolean enabled = true;
  private int priority = 5;
  private int pollingIntervalSeconds = 30;
  private boolean collectAshData = true;

  public String getCredentialName() {
    return credentialName;
  }

  public void setCredentialName(String credentialName) {
    this.credentialName = credentialName;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSslMode() {
    return sslMode;
  }

  public void setSslMode(String sslMode) {
    this.sslMode = sslMode;
  }

  public int getConnectionTimeoutMs() {
    return connectionTimeoutMs;
  }

  public void setConnectionTimeoutMs(int connectionTimeoutMs) {
    this.connectionTimeoutMs = connectionTimeoutMs;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public int getPollingIntervalSeconds() {
    return pollingIntervalSeconds;
  }

  public void setPollingIntervalSeconds(int pollingIntervalSeconds) {
    this.pollingIntervalSeconds = pollingIntervalSeconds;
  }

  public boolean isCollectAshData() {
    return collectAshData;
  }

  public void setCollectAshData(boolean collectAshData) {
    this.collectAshData = collectAshData;
  }
}
