package bf.gov.ascelc.logintegrite_backend.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Chiffres-clés forensiques du haut d'écran — alimente les 4 cartes KPI :
 * actions aujourd'hui, consultations 24h, utilisateurs actifs, alertes sécurité.
 *
 * <p>Les champs {@code delta*} et {@code *Precedent*} portent l'information de
 * tendance nécessaire aux indicateurs de variation (flèches ↑ / ↓ + pourcentage).
 * Toutes les valeurs proviennent de la fonction PostgreSQL
 * {@code audit.kpi_forensique(...)} exécutée en une seule requête.
 */
@Getter
@Builder
@AllArgsConstructor
public class KpiForensiqueResponse {

    // --- Carte 1 : Actions du jour ---------------------------------------------

    /** Nombre total d'actions tracées depuis 00h00 aujourd'hui. */
    private long actionsAujourdhuiTotal;

    /** Sous-total des créations tracées aujourd'hui. */
    private long actionsAujourdhuiCreation;

    /** Sous-total des modifications tracées aujourd'hui. */
    private long actionsAujourdhuiModification;

    /** Sous-total des suppressions tracées aujourd'hui. */
    private long actionsAujourdhuiSuppression;

    /**
     * Variation par rapport à la même tranche horaire d'hier.
     * Valeur positive = hausse ; négative = baisse.
     */
    private long deltaActionsVsHier;

    // --- Carte 2 : Consultations -----------------------------------------------

    /** Consultations tracées sur les 24 dernières heures glissantes. */
    private long consultations24h;

    /** Consultations sur la fenêtre 24h → 48h glissantes (base de comparaison). */
    private long consultations24hPrecedentes;

    /**
     * Variation en pourcentage entre les deux fenêtres.
     * {@code null} lorsque la fenêtre précédente est vide (division impossible).
     */
    private BigDecimal deltaConsultationsPct;

    // --- Carte 3 : Utilisateurs actifs -----------------------------------------

    /** Utilisateurs distincts ayant produit au moins une action ou une consultation sur 24h. */
    private long utilisateursActifs24h;

    /** Utilisateurs distincts actifs sur les 7 derniers jours. */
    private long utilisateursActifs7j;

    // --- Carte 4 : Alertes de sécurité -----------------------------------------

    /** Nombre total d'alertes forensiques ouvertes (agrégat des types ci-dessous). */
    private long alertesOuvertes;

    /** Actions produites hors heures ouvrables (avant 7h, après 18h, ou week-end). */
    private long actionsHorsHoraire24h;

    /** Utilisateurs ayant utilisé au moins 2 adresses IP distinctes en 24h. */
    private long ipsMultiples24h;
}
