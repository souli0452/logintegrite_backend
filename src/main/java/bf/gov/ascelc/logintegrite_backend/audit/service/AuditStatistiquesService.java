package bf.gov.ascelc.logintegrite_backend.audit.service;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.KpiForensiqueResponse;

/**
 * Service d'agrégations statistiques sur les journaux d'audit et de consultation.
 *
 * <p>Vague 1 : uniquement les KPI forensiques du haut d'écran.
 * <p>Vagues suivantes ajouteront :
 * <ul>
 *   <li>Vague 2 : heatmap d'activité 7×24 + détection d'anomalies + sessions ;</li>
 *   <li>Vague 3 : recherche avancée multicritères + export PV PDF.</li>
 * </ul>
 */
public interface AuditStatistiquesService {

    /**
     * KPI forensiques calculés en une seule requête PostgreSQL.
     *
     * @param heureOuverture heure de début de la plage ouvrable ASCE-LC (par défaut 7)
     * @param heureFermeture heure de fin de la plage ouvrable ASCE-LC (par défaut 18)
     */
    KpiForensiqueResponse kpiForensique(int heureOuverture, int heureFermeture);
}
