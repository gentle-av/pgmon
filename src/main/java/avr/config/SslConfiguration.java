package avr.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SslConfiguration {
    private final MonitoringConfig monitoringConfig;
    public SslConfiguration(MonitoringConfig monitoringConfig) {
        this.monitoringConfig = monitoringConfig;
    }
    @Bean
    public ServletWebServerFactory servletContainer() {
        ConfigRoot.ServerConfig serverConfig = monitoringConfig.getServerConfig();
        if (serverConfig != null && serverConfig.getSsl() != null && serverConfig.getSsl().isEnabled()) {
            TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
            tomcat.addAdditionalTomcatConnectors(createSslConnector());
            return tomcat;
        }
        return new TomcatServletWebServerFactory();
    }
    private Connector createSslConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("https");
        connector.setSecure(true);
        connector.setPort(8443);
        return connector;
    }
}
