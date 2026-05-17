package avr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.io.IOException;

@Configuration
public class SslConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SslConfiguration.class);

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerFactoryCustomizer(
            MonitoringConfig monitoringConfig) {

        return factory -> {
            ServerConfig serverConfig = monitoringConfig.getServer();
            if (serverConfig.ssl() != null && serverConfig.ssl().enabled()) {
                configureSsl(factory, serverConfig.ssl());
                log.info("✅ SSL включен для порта {}", serverConfig.port());
            } else {
                log.info("SSL отключен, используется HTTP на порту {}", serverConfig.port());
            }
        };
    }

    private void configureSsl(ConfigurableServletWebServerFactory factory, SslConfig sslConfig) {
        if (factory instanceof TomcatServletWebServerFactory tomcatFactory) {
            Ssl ssl = new Ssl();
            ssl.setEnabled(true);
            ssl.setKeyStore(getKeyStorePath(sslConfig.keyStore()));
            ssl.setKeyStorePassword(sslConfig.keyStorePassword());
            ssl.setKeyStoreType(sslConfig.keyStoreType() != null ? sslConfig.keyStoreType() : "PKCS12");
            if (sslConfig.keyAlias() != null) {
                ssl.setKeyAlias(sslConfig.keyAlias());
            }
            tomcatFactory.setSsl(ssl);
            log.info("SSL настроен с keystore: {}", sslConfig.keyStore());
        }
    }

    private String getKeyStorePath(String location) {
        if (location == null) {
            return null;
        }
        try {
            if (location.startsWith("classpath:")) {
                String path = location.substring("classpath:".length());
                ClassPathResource resource = new ClassPathResource(path);
                File file = resource.getFile();
                return file.getAbsolutePath();
            }
            return location;
        } catch (IOException e) {
            log.error("Ошибка загрузки keystore из {}: {}", location, e.getMessage());
            return location;
        }
    }
}
