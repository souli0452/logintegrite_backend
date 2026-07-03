package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
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
public interface PersonneMoraleRepository extends JpaRepository<PersonneMorale, UUID> {

    @EntityGraph(attributePaths = {"entite", "region"})
    Optional<PersonneMorale> findWithRelationsById(UUID id);

    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT m FROM PersonneMorale m WHERE m.validateurId = :validateurId AND " +
            "m.statutFiche IN ('ACTIVE', 'REJETE') ORDER BY m.dateValidation DESC")
    List<PersonneMorale> findRecentesByValidateur(@Param("validateurId") String validateurId, Pageable pageable);

    @EntityGraph(attributePaths = {"entite", "region"})
    @Override
    List<PersonneMorale> findAll();

    long countByStatutFiche(String statutFiche);

    // CORRIGÉ : même bug que côté PP (LOWER/CONCAT sur raisonSociale + statut
    // et typeStructure comparés directement, tous vulnérables sans CAST)
    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT m FROM PersonneMorale m WHERE " +
            "(CAST(:raisonSociale AS text) IS NULL OR LOWER(m.raisonSociale) LIKE LOWER(CONCAT('%', CAST(:raisonSociale AS text), '%'))) AND " +
            "(:entiteId IS NULL OR m.entite.id = :entiteId) AND " +
            "(:regionId IS NULL OR m.region.id = :regionId) AND " +
            "(CAST(:statut AS text) IS NULL OR m.statutFiche = :statut) AND " +
            "(CAST(:typeStructure AS text) IS NULL OR m.typeStructure = :typeStructure)")
    Page<PersonneMorale> rechercheAvancee(
            @Param("raisonSociale") String raisonSociale,
            @Param("entiteId") UUID entiteId,
            @Param("regionId") UUID regionId,
            @Param("statut") String statut,
            @Param("typeStructure") String typeStructure,
            Pageable pageable);

    @Query("SELECT m FROM PersonneMorale m WHERE m.statutFiche = 'ACTIVE' AND m.ifu = :ifu")
    Optional<PersonneMorale> findActiveByIfu(@Param("ifu") String ifu);

    @Query("SELECT m FROM PersonneMorale m WHERE m.statutFiche = 'ACTIVE' AND LOWER(m.raisonSociale) = LOWER(:raisonSociale)")
    Optional<PersonneMorale> findActiveByRaisonSociale(@Param("raisonSociale") String raisonSociale);

    // CORRIGÉ : CAST(:statut AS text)
    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT m FROM PersonneMorale m WHERE m.createdById = :createdById AND " +
            "(CAST(:statut AS text) IS NULL OR m.statutFiche = :statut)")
    Page<PersonneMorale> rechercheMesFiches(
            @Param("createdById") String createdById,
            @Param("statut") String statut,
            Pageable pageable);

    // CORRIGÉ : CAST(:dateDebut/:dateFin AS timestamp) — c'est l'autre moitié
    // du camembert PP/PM appelé par /statistiques
    @Query("SELECT COUNT(DISTINCT m) FROM PersonneMorale m " +
            "LEFT JOIN m.infractions i LEFT JOIN i.typeInfraction ti WHERE " +
            "(:regionId IS NULL OR m.region.id = :regionId) AND " +
            "(:entiteId IS NULL OR m.entite.id = :entiteId) AND " +
            "(:typeInfractionId IS NULL OR ti.id = :typeInfractionId) AND " +
            "(CAST(:dateDebut AS timestamp) IS NULL OR m.createdAt >= :dateDebut) AND " +
            "(CAST(:dateFin AS timestamp) IS NULL OR m.createdAt <= :dateFin)")
    long countFiltre(
            @Param("regionId") UUID regionId,
            @Param("entiteId") UUID entiteId,
            @Param("typeInfractionId") UUID typeInfractionId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT m FROM PersonneMorale m WHERE m.createdById = :userId AND " +
            "m.statutFiche IN ('BROUILLON', 'EN_ATTENTE_VALIDATION') ORDER BY m.updatedAt DESC")
    List<PersonneMorale> findRecentesByCreateur(@Param("userId") String userId, Pageable pageable);
}