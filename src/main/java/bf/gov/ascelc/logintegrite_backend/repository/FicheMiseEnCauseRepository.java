package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FicheMiseEnCauseRepository extends JpaRepository<FicheMiseEnCause, UUID> {

    @EntityGraph(attributePaths = {"entite", "region"})
    @Override
    Optional<FicheMiseEnCause> findById(UUID id);

    long countByStatutFiche(String statutFiche);

    // CORRIGÉ : CAST(:statut AS text) IS NULL — le paramètre statut, en String,
    // était sujet au même bug de type indéterminé côté PostgreSQL que dateDebut/dateFin
    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT f FROM FicheMiseEnCause f WHERE " +
            "(:entiteId IS NULL OR f.entite.id = :entiteId) AND " +
            "(:regionId IS NULL OR f.region.id = :regionId) AND " +
            "(CAST(:statut AS text) IS NULL OR f.statutFiche = :statut)")
    Page<FicheMiseEnCause> rechercheGlobale(
            @Param("entiteId") UUID entiteId,
            @Param("regionId") UUID regionId,
            @Param("statut") String statut,
            Pageable pageable);

    // CORRIGÉ : CAST(:recherche AS text) — même bug que "function lower(bytea)
    // does not exist" ci-dessous, appliqué ici en amont, avant même qu'il se déclenche
    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT f FROM FicheMiseEnCause f WHERE f.statutFiche = 'ACTIVE' AND " +
            "(:regionId IS NULL OR f.region.id = :regionId) AND " +
            "(:entiteId IS NULL OR f.entite.id = :entiteId) AND " +
            "(CAST(:recherche AS text) IS NULL OR :recherche = '' OR " +
            "   LOWER(COALESCE(TREAT(f AS PersonnePhysique).nom, '')) LIKE LOWER(CONCAT('%', CAST(:recherche AS text), '%')) OR " +
            "   LOWER(COALESCE(TREAT(f AS PersonnePhysique).prenoms, '')) LIKE LOWER(CONCAT('%', CAST(:recherche AS text), '%')) OR " +
            "   LOWER(COALESCE(TREAT(f AS PersonneMorale).raisonSociale, '')) LIKE LOWER(CONCAT('%', CAST(:recherche AS text), '%')))")
    Page<FicheMiseEnCause> rechercheRegistreOfficiel(
            @Param("regionId") UUID regionId,
            @Param("entiteId") UUID entiteId,
            @Param("recherche") String recherche,
            Pageable pageable);

    // CORRIGÉ : CAST(:dateDebut AS timestamp) / CAST(:dateFin AS timestamp)
    // — c'est exactement la requête de ta stack trace (paramètre $7)
    @Query("SELECT COUNT(DISTINCT f) FROM FicheMiseEnCause f " +
            "LEFT JOIN f.infractions i LEFT JOIN i.typeInfraction ti WHERE " +
            "(:regionId IS NULL OR f.region.id = :regionId) AND " +
            "(:entiteId IS NULL OR f.entite.id = :entiteId) AND " +
            "(:typeInfractionId IS NULL OR ti.id = :typeInfractionId) AND " +
            "(CAST(:dateDebut AS timestamp) IS NULL OR f.createdAt >= :dateDebut) AND " +
            "(CAST(:dateFin AS timestamp) IS NULL OR f.createdAt <= :dateFin)")
    long countTotalFiltre(
            @Param("regionId") UUID regionId,
            @Param("entiteId") UUID entiteId,
            @Param("typeInfractionId") UUID typeInfractionId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT COUNT(DISTINCT f) FROM FicheMiseEnCause f " +
            "LEFT JOIN f.infractions i LEFT JOIN i.typeInfraction ti WHERE " +
            "f.statutFiche = :statut AND " +
            "(:regionId IS NULL OR f.region.id = :regionId) AND " +
            "(:entiteId IS NULL OR f.entite.id = :entiteId) AND " +
            "(:typeInfractionId IS NULL OR ti.id = :typeInfractionId) AND " +
            "(CAST(:dateDebut AS timestamp) IS NULL OR f.createdAt >= :dateDebut) AND " +
            "(CAST(:dateFin AS timestamp) IS NULL OR f.createdAt <= :dateFin)")
    long countByStatutFiltre(
            @Param("statut") String statut,
            @Param("regionId") UUID regionId,
            @Param("entiteId") UUID entiteId,
            @Param("typeInfractionId") UUID typeInfractionId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT r.nom, COUNT(DISTINCT f) FROM FicheMiseEnCause f JOIN f.region r " +
            "LEFT JOIN f.infractions i LEFT JOIN i.typeInfraction ti WHERE " +
            "(:entiteId IS NULL OR f.entite.id = :entiteId) AND " +
            "(:typeInfractionId IS NULL OR ti.id = :typeInfractionId) AND " +
            "(CAST(:dateDebut AS timestamp) IS NULL OR f.createdAt >= :dateDebut) AND " +
            "(CAST(:dateFin AS timestamp) IS NULL OR f.createdAt <= :dateFin) " +
            "GROUP BY r.nom ORDER BY COUNT(DISTINCT f) DESC")
    List<Object[]> countGroupByRegion(
            @Param("entiteId") UUID entiteId,
            @Param("typeInfractionId") UUID typeInfractionId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT e.nom, COUNT(DISTINCT f) FROM FicheMiseEnCause f JOIN f.entite e " +
            "LEFT JOIN f.infractions i LEFT JOIN i.typeInfraction ti WHERE " +
            "(:regionId IS NULL OR f.region.id = :regionId) AND " +
            "(:typeInfractionId IS NULL OR ti.id = :typeInfractionId) AND " +
            "(CAST(:dateDebut AS timestamp) IS NULL OR f.createdAt >= :dateDebut) AND " +
            "(CAST(:dateFin AS timestamp) IS NULL OR f.createdAt <= :dateFin) " +
            "GROUP BY e.nom ORDER BY COUNT(DISTINCT f) DESC")
    List<Object[]> countGroupByEntite(
            @Param("regionId") UUID regionId,
            @Param("typeInfractionId") UUID typeInfractionId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT f.statutJudiciaire, COUNT(f) FROM FicheMiseEnCause f WHERE " +
            "(:regionId IS NULL OR f.region.id = :regionId) AND " +
            "(:entiteId IS NULL OR f.entite.id = :entiteId) AND " +
            "(CAST(:dateDebut AS timestamp) IS NULL OR f.createdAt >= :dateDebut) AND " +
            "(CAST(:dateFin AS timestamp) IS NULL OR f.createdAt <= :dateFin) " +
            "GROUP BY f.statutJudiciaire")
    List<Object[]> countGroupByStatutJudiciaire(
            @Param("regionId") UUID regionId,
            @Param("entiteId") UUID entiteId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);
}