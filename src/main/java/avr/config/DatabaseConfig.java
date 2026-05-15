package avr.config;

public record DatabaseConfig(
    String name,
    String host,
    int port,
    String database,
    String username,
    String password,
    boolean enabled,
    int monitoringIntervalSeconds) {
  public String getJdbcUrl() {
    return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
  }
}
