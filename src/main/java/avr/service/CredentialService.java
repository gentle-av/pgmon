package avr.service;

import avr.crypto.CryptoService;
import avr.model.StoredCredential;
import avr.repository.CredentialRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CredentialService {
    private final CredentialRepository credentialRepository;
    private final CryptoService cryptoService;
    private final Cache<String, String> passwordCache;

    public CredentialService(CredentialRepository credentialRepository, CryptoService cryptoService) {
        this.credentialRepository = credentialRepository;
        this.cryptoService = cryptoService;
        this.passwordCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(30))
                .maximumSize(100)
                .build();
    }

    public List<StoredCredential> getAllEnabledCredentials() {
        return credentialRepository.findByEnabledTrue();
    }

    public Optional<StoredCredential> getCredential(String id) {
        return credentialRepository.findById(id);
    }

    public Optional<StoredCredential> getCredentialByName(String name) {
        return credentialRepository.findByName(name);
    }

    public String getDecryptedPassword(String credentialId) throws Exception {
        String cached = passwordCache.getIfPresent(credentialId);
        if (cached != null) {
            return cached;
        }
        StoredCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found: " + credentialId));
        String decrypted = cryptoService.decrypt(credential.getEncryptedPassword());
        passwordCache.put(credentialId, decrypted);
        return decrypted;
    }

    public StoredCredential saveCredential(StoredCredential credential) throws Exception {
        if (credential.getId() == null) {
            credential.setId(UUID.randomUUID().toString());
        }
        if (credential.getCreatedAt() == null) {
            credential.setCreatedAt(java.time.LocalDateTime.now());
        }
        credential.setUpdatedAt(java.time.LocalDateTime.now());
        if (credential.getEncryptedPassword() != null && !credential.getEncryptedPassword().isEmpty()
                && !credential.getEncryptedPassword().contains(":")) {
            String encrypted = cryptoService.encrypt(credential.getEncryptedPassword());
            credential.setEncryptedPassword(encrypted);
        }
        return credentialRepository.save(credential);
    }

    public void deleteCredential(String id) {
        passwordCache.invalidate(id);
        credentialRepository.deleteById(id);
    }

    public void refreshCache(String credentialId) {
        passwordCache.invalidate(credentialId);
    }

    public StoredCredential saveCredentialWithoutEncryption(StoredCredential credential) {
        if (credential.getId() == null) {
            credential.setId(UUID.randomUUID().toString());
        }
        if (credential.getCreatedAt() == null) {
            credential.setCreatedAt(LocalDateTime.now());
        }
        credential.setUpdatedAt(LocalDateTime.now());
        return credentialRepository.save(credential);
    }
}
