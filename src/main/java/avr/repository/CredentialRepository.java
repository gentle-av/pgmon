package avr.repository;

import avr.model.StoredCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<StoredCredential, String> {
    List<StoredCredential> findByEnabledTrue();
    Optional<StoredCredential> findByName(String name);
}
