// dossier/repository/ImplicationRepository.java
package bf.gov.ascelc.logintegrite_backend.dossier.repository;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImplicationRepository extends JpaRepository<Implication, UUID> {
    List<Implication> findByDossierId(UUID dossierId);
    List<Implication> findByPersonneId(UUID personneId);
}
