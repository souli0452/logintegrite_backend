// dossier/repository/ImplicationFaitRepository.java (complet)
package bf.gov.ascelc.logintegrite_backend.dossier.repository;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.ImplicationFait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ImplicationFaitRepository extends JpaRepository<ImplicationFait, UUID> {

    List<ImplicationFait> findByImplicationId(UUID implicationId);
    List<ImplicationFait> findAllByImplication_PersonneId(UUID personneId);

    @Query("SELECT i.statutJudiciaire.libelle, COUNT(i) FROM ImplicationFait i GROUP BY i.statutJudiciaire.libelle")
    List<Object[]> compterParStatutJudiciaire();
}
