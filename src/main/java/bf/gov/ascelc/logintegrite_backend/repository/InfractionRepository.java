// InfractionRepository.java
package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.Infraction;
import bf.gov.ascelc.logintegrite_backend.entity.Infraction.NatureInfraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InfractionRepository
    extends JpaRepository<Infraction, Long> {

    List<Infraction> findByFicheId(Long ficheId);

    List<Infraction> findByNature(NatureInfraction nature);

    long countByNature(NatureInfraction nature);
}
