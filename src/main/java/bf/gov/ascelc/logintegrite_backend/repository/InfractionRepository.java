package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.Infraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface InfractionRepository extends JpaRepository<Infraction, UUID> { // Changé de Long à UUID pour correspondre à AuditEntity et au Service

    List<Infraction> findByNature(String nature);

    // Ajout de cette méthode indispensable pour filtrer les infractions par Fiche dans ton IHM
    List<Infraction> findByFicheId(UUID ficheId);
}