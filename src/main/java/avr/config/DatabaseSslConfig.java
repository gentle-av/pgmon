package avr.config;

public record DatabaseSslConfig(
    boolean enabled,
    String mode,
    String sslCert,
    String sslKey,
    String sslRootCert
) {
    public DatabaseSslConfig {
        if (mode == null) {
            mode = "require";
        }
    }

    public String getSslParams() {
        if (!enabled) return "";
        StringBuilder params = new StringBuilder("?ssl=true&sslmode=" + mode);
        if (sslCert != null && !sslCert.isEmpty()) {
            params.append("&sslcert=").append(sslCert);
        }
        if (sslKey != null && !sslKey.isEmpty()) {
            params.append("&sslkey=").append(sslKey);
        }
        if (sslRootCert != null && !sslRootCert.isEmpty()) {
            params.append("&sslrootcert=").append(sslRootCert);
        }
        return params.toString();
    }
}
