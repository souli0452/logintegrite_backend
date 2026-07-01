package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.StatutJudiciaireReferentiel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StatutJudiciaireReferentielRepository extends JpaRepository<StatutJudiciaireReferentiel, UUID> {
    List<StatutJudiciaireReferentiel> findByActifTrue();
}