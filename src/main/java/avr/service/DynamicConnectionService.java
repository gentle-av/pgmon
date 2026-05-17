package avr.service;

import avr.model.StoredCredential;
import avr.repository.CredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

@Service
public class DynamicConnectionService {
    private static final Logger log = LoggerFactory.getLogger(DynamicConnectionService.class);
    private final CredentialService credentialService;

    public DynamicConnectionService(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    public Connection createConnection(String credentialId) throws Exception {
        StoredCredential credential = credentialService.getCredential(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found: " + credentialId));
        String password = credentialService.getDecryptedPassword(credentialId);
        Properties props = new Properties();
        props.setProperty("user", credential.getUsername());
        props.setProperty("password", password);
        if (credential.getSslMode() != null && !"disable".equals(credential.getSslMode())) {
            props.setProperty("ssl", "true");
            props.setProperty("sslmode", credential.getSslMode());
        }
        return DriverManager.getConnection(credential.buildJdbcUrl(), props);
    }

    public boolean testConnection(String credentialId) {
        try (Connection conn = createConnection(credentialId);
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1");
            log.info("Connection test successful for credential: {}", credentialId);
            return true;
        } catch (Exception e) {
            log.error("Connection test failed for credential {}: {}", credentialId, e.getMessage());
            return false;
        }
    }
}
