// securite/repository/UtilisateurRoleRepository.java
package bf.gov.ascelc.logintegrite_backend.securite.repository;

import bf.gov.ascelc.logintegrite_backend.securite.entity.UtilisateurRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, UUID> {
    List<UtilisateurRole> findByUtilisateurId(UUID utilisateurId);
    boolean existsByUtilisateur_IdAndRoleHabilitation_Id(UUID utilisateurId, UUID roleHabilitationId);
    void deleteByUtilisateur_IdAndRoleHabilitation_Id(UUID utilisateurId, UUID roleHabilitationId);

}
