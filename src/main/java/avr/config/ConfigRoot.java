package avr.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ConfigRoot {
    private ServerConfig server;
    private StorageConfig storage;
    private WebConfig web;
    private List<DatabaseConfig> databases;
    private MonitoringSettings monitoring;
    private AlertingSettings alerting;
    public ServerConfig getServer() { return server; }
    public void setServer(ServerConfig server) { this.server = server; }
    public StorageConfig getStorage() { return storage; }
    public void setStorage(StorageConfig storage) { this.storage = storage; }
    public WebConfig getWeb() { return web; }
    public void setWeb(WebConfig web) { this.web = web; }
    public List<DatabaseConfig> getDatabases() { return databases; }
    public void setDatabases(List<DatabaseConfig> databases) { this.databases = databases; }
    public MonitoringSettings getMonitoring() { return monitoring; }
    public void setMonitoring(MonitoringSettings monitoring) { this.monitoring = monitoring; }
    public AlertingSettings getAlerting() { return alerting; }
    public void setAlerting(AlertingSettings alerting) { this.alerting = alerting; }

    public static class StorageConfig {
        private String type;
        private String host;
        private Integer port;
        private String database;
        private String username;
        private String password;
        private Integer poolSize;
        private String schema;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }
        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Integer getPoolSize() { return poolSize; }
        public void setPoolSize(Integer poolSize) { this.poolSize = poolSize; }
        public String getSchema() { return schema; }
        public void setSchema(String schema) { this.schema = schema; }

        public String getJdbcUrl() {
            return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        }
    }

    public static class ServerConfig {
        private int port;
        private SslConfig ssl;
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public SslConfig getSsl() { return ssl; }
        public void setSsl(SslConfig ssl) { this.ssl = ssl; }
    }

    public static class SslConfig {
        private boolean enabled;
        private String mode;
        private String sslCert;
        private String sslKey;
        private String sslRootCert;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getSslCert() { return sslCert; }
        public void setSslCert(String sslCert) { this.sslCert = sslCert; }
        public String getSslKey() { return sslKey; }
        public void setSslKey(String sslKey) { this.sslKey = sslKey; }
        public String getSslRootCert() { return sslRootCert; }
        public void setSslRootCert(String sslRootCert) { this.sslRootCert = sslRootCert; }
    }

    public static class WebConfig {
        private boolean enabled;
        private String staticPath;
        private String title;
        private int refreshIntervalSeconds;
        private String theme;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getStaticPath() { return staticPath; }
        public void setStaticPath(String staticPath) { this.staticPath = staticPath; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public int getRefreshIntervalSeconds() { return refreshIntervalSeconds; }
        public void setRefreshIntervalSeconds(int refreshIntervalSeconds) { this.refreshIntervalSeconds = refreshIntervalSeconds; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
    }

    public static class DatabaseConfig {
        private String name;
        private String host;
        private int port;
        private String database;
        private String username;
        private String password;
        private boolean enabled;
        private int monitoringIntervalSeconds;
        private SslConfig ssl;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMonitoringIntervalSeconds() { return monitoringIntervalSeconds; }
        public void setMonitoringIntervalSeconds(int monitoringIntervalSeconds) { this.monitoringIntervalSeconds = monitoringIntervalSeconds; }
        public SslConfig getSsl() { return ssl; }
        public void setSsl(SslConfig ssl) { this.ssl = ssl; }
    }

    public static class MonitoringSettings {
        private int defaultIntervalSeconds;
        private boolean collectQueries;
        private boolean collectConnections;
        private boolean collectLocks;
        private boolean collectCacheHitRatio;
        private boolean collectTableSizes;
        private int slowQueryThresholdMs;
        private int maxSlowQueries;
        private SslConfig ssl;
        public SslConfig getSsl() { return ssl; }
        public void setSsl(SslConfig ssl) { this.ssl = ssl; }
        public int getDefaultIntervalSeconds() { return defaultIntervalSeconds; }
        public void setDefaultIntervalSeconds(int defaultIntervalSeconds) { this.defaultIntervalSeconds = defaultIntervalSeconds; }
        public boolean isCollectQueries() { return collectQueries; }
        public void setCollectQueries(boolean collectQueries) { this.collectQueries = collectQueries; }
        public boolean isCollectConnections() { return collectConnections; }
        public void setCollectConnections(boolean collectConnections) { this.collectConnections = collectConnections; }
        public boolean isCollectLocks() { return collectLocks; }
        public void setCollectLocks(boolean collectLocks) { this.collectLocks = collectLocks; }
        public boolean isCollectCacheHitRatio() { return collectCacheHitRatio; }
        public void setCollectCacheHitRatio(boolean collectCacheHitRatio) { this.collectCacheHitRatio = collectCacheHitRatio; }
        public boolean isCollectTableSizes() { return collectTableSizes; }
        public void setCollectTableSizes(boolean collectTableSizes) { this.collectTableSizes = collectTableSizes; }
        public int getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
        public void setSlowQueryThresholdMs(int slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }
        public int getMaxSlowQueries() { return maxSlowQueries; }
        public void setMaxSlowQueries(int maxSlowQueries) { this.maxSlowQueries = maxSlowQueries; }
    }

    public static class AlertingSettings {
        private boolean enabled;
        private String webhookUrl;
        private ConnectionThresholds connectionThresholds;
        private int lockThresholdSeconds;
        private int cacheHitRatioMinPercent;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
        public ConnectionThresholds getConnectionThresholds() { return connectionThresholds; }
        public void setConnectionThresholds(ConnectionThresholds connectionThresholds) { this.connectionThresholds = connectionThresholds; }
        public int getLockThresholdSeconds() { return lockThresholdSeconds; }
        public void setLockThresholdSeconds(int lockThresholdSeconds) { this.lockThresholdSeconds = lockThresholdSeconds; }
        public int getCacheHitRatioMinPercent() { return cacheHitRatioMinPercent; }
        public void setCacheHitRatioMinPercent(int cacheHitRatioMinPercent) { this.cacheHitRatioMinPercent = cacheHitRatioMinPercent; }
    }

    public static class ConnectionThresholds {
        private int maxConnections;
        private int minAvailableConnections;
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        public int getMinAvailableConnections() { return minAvailableConnections; }
        public void setMinAvailableConnections(int minAvailableConnections) { this.minAvailableConnections = minAvailableConnections; }
    }
}
