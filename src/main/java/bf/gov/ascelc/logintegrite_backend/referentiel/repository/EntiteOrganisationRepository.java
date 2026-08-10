// referentiel/repository/EntiteOrganisationRepository.java
package bf.gov.ascelc.logintegrite_backend.referentiel.repository;

import bf.gov.ascelc.logintegrite_backend.referentiel.entity.EntiteOrganisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EntiteOrganisationRepository extends JpaRepository<EntiteOrganisation, UUID> {
}
