package bf.gov.ascelc.logintegrite_backend.personne.repository;

import bf.gov.ascelc.logintegrite_backend.personne.entity.PieceIdentite;
import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePieceIdentite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PieceIdentiteRepository extends JpaRepository<PieceIdentite, UUID> {

    List<PieceIdentite> findByPersonnePhysiqueId(UUID personnePhysiqueId);

    // ─── Vérification d'unicité : création (via enum & référentiel) ─────────
    boolean existsByTypePieceAndNumero(TypePieceIdentite typePiece, String numero);
    boolean existsByTypePieceRef_IdAndNumero(UUID typePieceRefId, String numero);

    // ─── Vérification d'unicité : modification (pour comparer les ID) ───────
    Optional<PieceIdentite> findByTypePieceAndNumero(TypePieceIdentite typePiece, String numero);
    Optional<PieceIdentite> findByTypePieceRef_IdAndNumero(UUID typePieceRefId, String numero);
}
