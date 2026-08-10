// referentiel/repository/StatutJudiciaireRepository.java
package bf.gov.ascelc.logintegrite_backend.referentiel.repository;

import bf.gov.ascelc.logintegrite_backend.referentiel.entity.StatutJudiciaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StatutJudiciaireRepository extends JpaRepository<StatutJudiciaire, UUID> {
    Optional<StatutJudiciaire> findByLibelleIgnoreCase(String libelle);
}
