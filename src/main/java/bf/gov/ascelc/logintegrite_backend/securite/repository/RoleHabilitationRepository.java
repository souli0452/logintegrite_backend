// securite/repository/RoleHabilitationRepository.java
package bf.gov.ascelc.logintegrite_backend.securite.repository;

import java.util.Optional;
import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import bf.gov.ascelc.logintegrite_backend.securite.entity.RoleHabilitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleHabilitationRepository extends JpaRepository<RoleHabilitation, UUID> {
Optional<RoleHabilitation> findByCode(CodeRole code);
}
