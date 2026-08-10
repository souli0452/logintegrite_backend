package bf.gov.ascelc.logintegrite_backend.securite.repository;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {
    Optional<Utilisateur> findByKeycloakId(String keycloakId);
    boolean existsByEmailIgnoreCase(String email);
}
