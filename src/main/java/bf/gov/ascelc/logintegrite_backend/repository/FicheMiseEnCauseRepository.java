package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.StatutFiche;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository générique — opère sur les deux types (PP + PM)
 * via la table fiche_mise_en_cause.
 * Le @SQLRestriction("deleted = false") de l'entité parente
 * filtre automatiquement les enregistrements supprimés.
 */
@Repository
public interface FicheMiseEnCauseRepository
    extends JpaRepository<FicheMiseEnCause, Long> {

    // ── Chargement optimisé des relations pour une fiche unique ──
    @Query("""
        SELECT f FROM FicheMiseEnCause f 
        LEFT JOIN FETCH f.entite 
        LEFT JOIN FETCH f.region 
        WHERE f.id = :id
    """)
    Optional<FicheMiseEnCause> findByIdWithRelations(@Param("id") Long id);

    // ── Chargement optimisé pour les exports de rapports (Évite LazyInitializationException sur toutes les relations) ──
    @Query("""
        SELECT DISTINCT f FROM FicheMiseEnCause f
        LEFT JOIN FETCH f.entite
        LEFT JOIN FETCH f.region
        LEFT JOIN FETCH f.infractions
        WHERE f.statutFiche = 'ACTIVE'
    """)
    List<FicheMiseEnCause> findAllActivesWithAssociations();

    // ── Compteurs pour tableau de bord ────────────────────────
    long countByStatutFiche(StatutFiche statut);

    // ── Workflow ──────────────────────────────────────────────
    Page<FicheMiseEnCause> findByStatutFiche(
        StatutFiche statut, Pageable pageable);

    // ── Recherche globale multicritères (PP + PM) ─────────────
    @Query("""
        SELECT f FROM FicheMiseEnCause f
        WHERE f.statutFiche = 'ACTIVE'
          AND (:entiteId IS NULL OR f.entite.id = :entiteId)
          AND (:regionId IS NULL OR f.region.id = :regionId)
          AND (:statut IS NULL OR
               CAST(f.statutJudiciaire AS string) = :statut)
    """)
    Page<FicheMiseEnCause> rechercheGlobale(
        @Param("entiteId") Long entiteId,
        @Param("regionId") Long regionId,
        @Param("statut") String statut,
        Pageable pageable
    );

    // ── Statistiques par statut judiciaire ────────────────────
    @Query("SELECT f.statutJudiciaire, COUNT(f) " +
           "FROM FicheMiseEnCause f " +
           "WHERE f.statutFiche = 'ACTIVE' " +
           "GROUP BY f.statutJudiciaire")
    List<Object[]> statistiquesParStatut();

    // ── Top entités (PP + PM confondus) ───────────────────────
    @Query("SELECT f.entite.nom, COUNT(f) " +
           "FROM FicheMiseEnCause f " +
           "WHERE f.statutFiche = 'ACTIVE' " +
           "AND f.entite IS NOT NULL " +
           "GROUP BY f.entite.nom ORDER BY COUNT(f) DESC")
    List<Object[]> top5Entites(Pageable pageable);
}
