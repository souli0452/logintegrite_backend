package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonnePhysiqueRepository extends JpaRepository<PersonnePhysique, UUID> {

    @EntityGraph(attributePaths = {"entite", "region"})
    Optional<PersonnePhysique> findWithRelationsById(UUID id);

    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT p FROM PersonnePhysique p WHERE p.validateurId = :validateurId AND " +
            "p.statutFiche IN ('ACTIVE', 'REJETE') ORDER BY p.dateValidation DESC")
    List<PersonnePhysique> findRecentesByValidateur(@Param("validateurId") String validateurId, Pageable pageable);

    @EntityGraph(attributePaths = {"entite", "region"})
    @Override
    List<PersonnePhysique> findAll();

    @EntityGraph(attributePaths = {"entite", "region"})
    Page<PersonnePhysique> findByStatutFiche(String statutFiche, Pageable pageable);

    boolean existsByMatricule(String matricule);

    long countByStatutFiche(String statutFiche);

    // CORRIGÉ : c'est CETTE requête qui produisait "function lower(bytea) does
    // not exist" — le paramètre :nom, utilisé dans LOWER(CONCAT(...)) sans
    // jamais être casté, était parfois lié à null (recherche sans filtre nom).
    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT p FROM PersonnePhysique p WHERE " +
            "(CAST(:nom AS text) IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%', CAST(:nom AS text), '%')) OR LOWER(p.prenoms) LIKE LOWER(CONCAT('%', CAST(:nom AS text), '%'))) AND " +
            "(:entiteId IS NULL OR p.entite.id = :entiteId) AND " +
            "(:regionId IS NULL OR p.region.id = :regionId) AND " +
            "(CAST(:statut AS text) IS NULL OR p.statutFiche = :statut)")
    Page<PersonnePhysique> rechercheAvancee(
            @Param("nom") String nom,
            @Param("entiteId") UUID entiteId,
            @Param("regionId") UUID regionId,
            @Param("statut") String statut,
            Pageable pageable);

    @Query("SELECT p FROM PersonnePhysique p WHERE p.statutFiche = 'ACTIVE' AND p.matricule = :matricule")
    Optional<PersonnePhysique> findActiveByMatricule(@Param("matricule") String matricule);

    @Query("SELECT p FROM PersonnePhysique p WHERE p.statutFiche = 'ACTIVE' AND " +
            "LOWER(p.nom) = LOWER(:nom) AND LOWER(p.prenoms) = LOWER(:prenoms) AND p.dateNaissance = :dateNaissance")
    Optional<PersonnePhysique> findActiveByIdentite(
            @Param("nom") String nom,
            @Param("prenoms") String prenoms,
            @Param("dateNaissance") LocalDate dateNaissance);

    // CORRIGÉ : CAST(:statut AS text)
    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT p FROM PersonnePhysique p WHERE p.createdById = :createdById AND " +
            "(CAST(:statut AS text) IS NULL OR p.statutFiche = :statut)")
    Page<PersonnePhysique> rechercheMesFiches(
            @Param("createdById") String createdById,
            @Param("statut") String statut,
            Pageable pageable);

    // CORRIGÉ : CAST(:dateDebut/:dateFin AS timestamp) — appelée par
    // /statistiques pour le camembert PP/PM, même bug que countTotalFiltre
    @Query("SELECT COUNT(DISTINCT p) FROM PersonnePhysique p " +
            "LEFT JOIN p.infractions i LEFT JOIN i.typeInfraction ti WHERE " +
            "(:regionId IS NULL OR p.region.id = :regionId) AND " +
            "(:entiteId IS NULL OR p.entite.id = :entiteId) AND " +
            "(:typeInfractionId IS NULL OR ti.id = :typeInfractionId) AND " +
            "(CAST(:dateDebut AS timestamp) IS NULL OR p.createdAt >= :dateDebut) AND " +
            "(CAST(:dateFin AS timestamp) IS NULL OR p.createdAt <= :dateFin)")
    long countFiltre(
            @Param("regionId") UUID regionId,
            @Param("entiteId") UUID entiteId,
            @Param("typeInfractionId") UUID typeInfractionId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT p FROM PersonnePhysique p WHERE p.createdById = :userId AND " +
            "p.statutFiche IN ('BROUILLON', 'EN_ATTENTE_VALIDATION') ORDER BY p.updatedAt DESC")
    List<PersonnePhysique> findRecentesByCreateur(@Param("userId") String userId, Pageable pageable);
}