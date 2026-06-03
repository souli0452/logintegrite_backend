// PieceJointeRepository.java
package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PieceJointeRepository
    extends JpaRepository<PieceJointe, Long> {

    List<PieceJointe> findByFicheId(Long ficheId);

    void deleteByFicheId(Long ficheId);
}
