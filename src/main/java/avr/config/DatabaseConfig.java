package avr.config;

public record DatabaseConfig(
    String name,
    String host,
    int port,
    String database,
    String username,
    String password,
    boolean enabled,
    int monitoringIntervalSeconds,
    DatabaseSslConfig ssl
) {
    public DatabaseConfig {
        if (ssl == null) {
            ssl = new DatabaseSslConfig(false, "require", null, null, null);
        }
    }

    public String getJdbcUrl() {
        String baseUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        String sslParams = ssl.getSslParams();
        return baseUrl + sslParams;
    }
}
