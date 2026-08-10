package bf.gov.ascelc.logintegrite_backend.referentiel.repository;

import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypePieceIdentiteRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TypePieceIdentiteRefRepository extends JpaRepository<TypePieceIdentiteRef, UUID> {

    List<TypePieceIdentiteRef> findByActifTrueOrderByLibelleAsc();

    Optional<TypePieceIdentiteRef> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
