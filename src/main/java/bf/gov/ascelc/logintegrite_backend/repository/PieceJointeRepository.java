package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PieceJointeRepository extends JpaRepository<PieceJointe, UUID> {

    List<PieceJointe> findByFicheId(UUID ficheId);

    // AJOUT : liste des pièces jointes rattachées à une infraction précise
    List<PieceJointe> findByInfractionId(UUID infractionId);
}