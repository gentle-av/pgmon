package avr.controller;

import avr.model.StoredCredential;
import avr.service.CredentialService;
import avr.service.DynamicConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credentials")
public class CredentialController {
    private final CredentialService credentialService;
    private final DynamicConnectionService connectionService;

    public CredentialController(CredentialService credentialService, DynamicConnectionService connectionService) {
        this.credentialService = credentialService;
        this.connectionService = connectionService;
    }

    @GetMapping
    public List<StoredCredential> getAllCredentials() {
        return credentialService.getAllEnabledCredentials();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoredCredential> getCredential(@PathVariable String id) {
        return credentialService.getCredential(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public StoredCredential createCredential(@RequestBody StoredCredential credential) throws Exception {
        return credentialService.saveCredential(credential);
    }

    @PutMapping("/{id}")
    public StoredCredential updateCredential(@PathVariable String id, @RequestBody StoredCredential credential) throws Exception {
        credential.setId(id);
        return credentialService.saveCredential(credential);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCredential(@PathVariable String id) {
        credentialService.deleteCredential(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable String id) {
        boolean success = connectionService.testConnection(id);
        Map<String, Object> response = new HashMap<>();
        response.put("credentialId", id);
        response.put("success", success);
        if (success) {
            response.put("message", "Connection successful");
        } else {
            response.put("message", "Connection failed");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refresh")
    public ResponseEntity<Map<String, String>> refreshPassword(@PathVariable String id) {
        credentialService.refreshCache(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "refreshed");
        response.put("credentialId", id);
        return ResponseEntity.ok(response);
    }
}
