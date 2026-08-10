package bf.gov.ascelc.logintegrite_backend.audit.repository;

import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalAudit;
import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalAuditId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repository dédié aux requêtes forensiques du journal d'audit.
 *
 * <p>Les requêtes exposées ici sont toutes des <strong>native queries</strong>
 * s'appuyant sur les fonctions PostgreSQL du schéma {@code audit} définies
 * dans la migration {@code V202608070001__audit_forensique_fonctions.sql} :
 * <ul>
 *   <li>{@code audit.etat_chaine()}</li>
 *   <li>{@code audit.verifier_integrite_chaine(?)}</li>
 *   <li>{@code audit.verifier_maillon(?)}</li>
 *   <li>{@code audit.kpi_forensique(?, ?)}</li>
 * </ul>
 *
 * <p>Le repository JPA « standard » ({@link JournalAuditRepository}) est
 * volontairement laissé intact — cette classe ajoute les capacités forensiques
 * sans modifier les points d'entrée existants.
 */
@Repository
public interface JournalAuditForensiqueRepository extends JpaRepository<JournalAudit, JournalAuditId> {

    // -------------------------------------------------------------------------
    // 1. État de la chaîne (bandeau d'intégrité)
    // -------------------------------------------------------------------------
    /**
     * Retourne l'état courant de la chaîne d'audit sous forme de
     * {@code Map<colonne, valeur>}. Une seule ligne est renvoyée.
     */
    @Query(value = """
            SELECT total_entrees,
                   dernier_hash,
                   date_derniere_action,
                   date_premiere_action,
                   total_utilisateurs,
                   total_entites
            FROM audit.etat_chaine()
            """, nativeQuery = true)
    Map<String, Object> etatChaine();

    // -------------------------------------------------------------------------
    // 2. Vérification complète de la chaîne
    // -------------------------------------------------------------------------
    /**
     * Retourne uniquement les maillons rompus détectés par la fonction SQL
     * {@code audit.verifier_integrite_chaine()}. Liste vide = chaîne intacte.
     *
     * @param limiteLignes nombre maximum de maillons à vérifier depuis le début
     *                     de la chaîne ; {@code null} pour vérifier la totalité.
     */
    @Query(value = """
            SELECT id,
                   date_action,
                   action,
                   entite_cible,
                   hash_attendu,
                   hash_stocke,
                   hash_precedent,
                   hash_precedent_attendu,
                   type_rupture
            FROM audit.verifier_integrite_chaine(:limiteLignes)
            """, nativeQuery = true)
    List<Map<String, Object>> verifierIntegriteChaine(@Param("limiteLignes") Long limiteLignes);

    // -------------------------------------------------------------------------
    // 3. Vérification d'un maillon isolé (par hash exact ou préfixe)
    // -------------------------------------------------------------------------
    /**
     * Localise un maillon par son hash (complet ou préfixe hexadécimal) et
     * en retourne l'état de vérification. Retourne {@code null} si aucun
     * maillon ne correspond.
     */
    @Query(value = """
            SELECT id,
                   date_action,
                   action,
                   entite_cible,
                   hash_stocke,
                   hash_recalcule,
                   hash_precedent,
                   integre,
                   position_dans_chaine
            FROM audit.verifier_maillon(:hash)
            """, nativeQuery = true)
    Map<String, Object> verifierMaillon(@Param("hash") String hash);

    // -------------------------------------------------------------------------
    // 4. KPI forensiques
    // -------------------------------------------------------------------------
    /**
     * Retourne les KPI forensiques calculés en une seule requête serveur.
     *
     * @param heureOuverture   heure de début de plage ouvrable (par défaut 7)
     * @param heureFermeture   heure de fin de plage ouvrable (par défaut 18)
     */
    @Query(value = """
            SELECT actions_aujourdhui_total,
                   actions_aujourdhui_creation,
                   actions_aujourdhui_modif,
                   actions_aujourdhui_suppr,
                   delta_actions_vs_hier,
                   consultations_24h,
                   consultations_24h_precedentes,
                   delta_consultations_pct,
                   utilisateurs_actifs_24h,
                   utilisateurs_actifs_7j,
                   alertes_ouvertes,
                   actions_hors_horaire_24h,
                   ips_multiples_24h
            FROM audit.kpi_forensique(:heureOuverture, :heureFermeture)
            """, nativeQuery = true)
    Map<String, Object> kpiForensique(
            @Param("heureOuverture") int heureOuverture,
            @Param("heureFermeture") int heureFermeture
    );
}
