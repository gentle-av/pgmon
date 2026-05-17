package avr.config;

public record SslConfig(
    boolean enabled,
    String keyStore,
    String keyStorePassword,
    String keyStoreType,
    String keyAlias
) {}
