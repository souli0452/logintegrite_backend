// HistoriqueStatutRepository.java
package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoriqueStatutRepository
    extends JpaRepository<HistoriqueStatut, Long> {

    List<HistoriqueStatut> findByFicheIdOrderByDateChangementDesc(
        Long ficheId);
}
