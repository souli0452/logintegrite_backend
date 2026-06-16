package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Long> {

    @Query(value = "SELECT * FROM historique_statut WHERE fiche_id = :ficheId ORDER BY date_changement DESC", nativeQuery = true)
    List<HistoriqueStatut> findByFicheIdOrderByDateChangementDesc(@Param("ficheId") Long ficheId);
}