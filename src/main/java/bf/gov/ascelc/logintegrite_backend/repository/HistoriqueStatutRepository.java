package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, UUID> {

    // Permet d'afficher la frise chronologique des changements d'état d'un dossier sur l'IHM
    List<HistoriqueStatut> findByFicheIdOrderByCreatedAtDesc(UUID ficheId);
}