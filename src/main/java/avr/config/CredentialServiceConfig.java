package avr.config;

import avr.crypto.CryptoService;
import avr.repository.CredentialRepository;
import avr.service.CredentialService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
public class CredentialServiceConfig {
    @Bean
    public CredentialService credentialService(CredentialRepository credentialRepository, CryptoService cryptoService) {
        return new CredentialService(credentialRepository, cryptoService);
    }
}
