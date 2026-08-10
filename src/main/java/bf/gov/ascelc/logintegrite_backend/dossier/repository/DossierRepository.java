// dossier/repository/DossierRepository.java (complet)
package bf.gov.ascelc.logintegrite_backend.dossier.repository;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutDossier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DossierRepository extends JpaRepository<Dossier, UUID> {
    long countByStatutDossier(StatutDossier statut);
}
