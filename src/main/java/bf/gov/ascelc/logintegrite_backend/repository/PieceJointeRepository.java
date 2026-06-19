package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PieceJointeRepository extends JpaRepository<PieceJointe, UUID> {

    // Pour lister efficacement tous les justificatifs d'une fiche donnée
    List<PieceJointe> findByFicheId(UUID ficheId);
}