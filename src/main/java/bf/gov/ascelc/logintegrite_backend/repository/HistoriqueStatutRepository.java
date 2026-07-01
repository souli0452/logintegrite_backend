package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, UUID> {

    List<HistoriqueStatut> findByFicheIdOrderByCreatedAtDesc(UUID ficheId);

    // AJOUT : historique propre à une infraction précise
    List<HistoriqueStatut> findByInfractionIdOrderByCreatedAtDesc(UUID infractionId);
}