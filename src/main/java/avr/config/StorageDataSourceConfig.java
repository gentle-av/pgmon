package avr.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;

@Configuration
public class StorageDataSourceConfig {
    private static final Logger log = LoggerFactory.getLogger(StorageDataSourceConfig.class);
    private final Json5Parser json5Parser;
    private ConfigRoot.StorageConfig storageConfig;
    public StorageDataSourceConfig(Json5Parser json5Parser) {
        this.json5Parser = json5Parser;
    }
    @PostConstruct
    public void init() throws Exception {
        File configFile = new File("monitoring-config.json5");
        if (!configFile.exists()) {
            configFile = new ClassPathResource("monitoring-config.json5").getFile();
        }
        try (FileInputStream fis = new FileInputStream(configFile)) {
            ConfigRoot configRoot = json5Parser.parse(fis, ConfigRoot.class);
            this.storageConfig = configRoot.getStorage();
        }
        if (storageConfig.getPassword() != null && storageConfig.getPassword().startsWith("${")) {
            String envVar = storageConfig.getPassword().substring(2, storageConfig.getPassword().length() - 1);
            storageConfig.setPassword(System.getenv(envVar));
        }
        log.info("Storage БД: {}/{}", storageConfig.getHost(), storageConfig.getDatabase());
    }
    @Bean
    public DataSource storageDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(storageConfig.getJdbcUrl());
        config.setUsername(storageConfig.getUsername());
        config.setPassword(storageConfig.getPassword());
        config.setMaximumPoolSize(storageConfig.getPoolSize() != null ? storageConfig.getPoolSize() : 10);
        config.setMinimumIdle(2);
        config.setPoolName("StorageHikariPool");
        return new HikariDataSource(config);
    }
    public ConfigRoot.StorageConfig getStorageConfig() {
        return storageConfig;
    }
}
