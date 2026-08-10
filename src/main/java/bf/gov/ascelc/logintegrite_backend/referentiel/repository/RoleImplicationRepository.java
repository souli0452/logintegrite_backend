// referentiel/repository/RoleImplicationRepository.java
package bf.gov.ascelc.logintegrite_backend.referentiel.repository;

import bf.gov.ascelc.logintegrite_backend.referentiel.entity.RoleImplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleImplicationRepository extends JpaRepository<RoleImplication, UUID> {
}
