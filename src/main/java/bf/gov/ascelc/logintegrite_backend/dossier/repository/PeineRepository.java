// dossier/repository/PeineRepository.java
package bf.gov.ascelc.logintegrite_backend.dossier.repository;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.Peine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PeineRepository extends JpaRepository<Peine, UUID> {
    List<Peine> findByImplicationFaitId(UUID implicationFaitId);
}
