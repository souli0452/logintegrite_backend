// referentiel/repository/SourceSignalementRepository.java
package bf.gov.ascelc.logintegrite_backend.referentiel.repository;

import bf.gov.ascelc.logintegrite_backend.referentiel.entity.SourceSignalement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SourceSignalementRepository extends JpaRepository<SourceSignalement, UUID> {
}
