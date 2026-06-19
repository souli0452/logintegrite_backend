package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.Sauvegarde;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SauvegardeRepository extends JpaRepository<Sauvegarde, UUID> {

    // Permet à l'administrateur de lister l'historique du plus récent au plus ancien
    List<Sauvegarde> findAllByOrderByDateDebutDesc();
}