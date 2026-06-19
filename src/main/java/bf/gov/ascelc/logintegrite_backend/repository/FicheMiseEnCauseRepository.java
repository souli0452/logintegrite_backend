package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FicheMiseEnCauseRepository extends JpaRepository<FicheMiseEnCause, UUID> {

    @Query("SELECT f FROM FicheMiseEnCause f LEFT JOIN FETCH f.infractions LEFT JOIN FETCH f.piecesJointes WHERE f.id = :id")
    Optional<FicheMiseEnCause> findByIdWithRelations(@Param("id") UUID id);

    long countByStatutFiche(String statutFiche);

    @Query("SELECT f FROM FicheMiseEnCause f WHERE " +
            "(:entiteId IS NULL OR f.entite.id = :entiteId) AND " +
            "(:regionId IS NULL OR f.region.id = :regionId) AND " +
            "(:statut IS NULL OR f.statutFiche = :statut)")
    Page<FicheMiseEnCause> rechercheGlobale(
            @Param("entiteId") UUID entiteId,
            @Param("regionId") UUID regionId,
            @Param("statut") String statut,
            Pageable pageable);
}