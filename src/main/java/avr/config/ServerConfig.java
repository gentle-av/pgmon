package avr.config;

public record ServerConfig(int port, SslConfig ssl) {
    public ServerConfig {
        if (ssl == null) {
            ssl = new SslConfig(false, null, null, null, null);
        }
    }
}
