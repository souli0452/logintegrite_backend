// EntiteOrganisationRepository.java
package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.EntiteOrganisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EntiteOrganisationRepository
    extends JpaRepository<EntiteOrganisation, Long> {
    List<EntiteOrganisation> findByActifTrue();
}
