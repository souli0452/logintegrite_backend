package bf.gov.ascelc.logintegrite_backend.audit.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.KpiForensiqueResponse;
import bf.gov.ascelc.logintegrite_backend.audit.repository.JournalAuditForensiqueRepository;
import bf.gov.ascelc.logintegrite_backend.audit.service.AuditStatistiquesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Implémentation des statistiques forensiques.
 *
 * <p>Vague 1 : les KPI proviennent intégralement de la fonction PostgreSQL
 * {@code audit.kpi_forensique(?, ?)}. Aucune agrégation Java — on garde le calcul
 * proche de la donnée pour ne pas transférer des millions de lignes.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditStatistiquesServiceImpl implements AuditStatistiquesService {

    private final JournalAuditForensiqueRepository repository;

    @Override
    public KpiForensiqueResponse kpiForensique(int heureOuverture, int heureFermeture) {
        Map<String, Object> ligne = repository.kpiForensique(heureOuverture, heureFermeture);
        if (ligne == null) {
            return KpiForensiqueResponse.builder().build();
        }
        return KpiForensiqueResponse.builder()
                .actionsAujourdhuiTotal(lireLong(ligne, "actions_aujourdhui_total"))
                .actionsAujourdhuiCreation(lireLong(ligne, "actions_aujourdhui_creation"))
                .actionsAujourdhuiModification(lireLong(ligne, "actions_aujourdhui_modif"))
                .actionsAujourdhuiSuppression(lireLong(ligne, "actions_aujourdhui_suppr"))
                .deltaActionsVsHier(lireLong(ligne, "delta_actions_vs_hier"))
                .consultations24h(lireLong(ligne, "consultations_24h"))
                .consultations24hPrecedentes(lireLong(ligne, "consultations_24h_precedentes"))
                .deltaConsultationsPct(lireBigDecimal(ligne, "delta_consultations_pct"))
                .utilisateursActifs24h(lireLong(ligne, "utilisateurs_actifs_24h"))
                .utilisateursActifs7j(lireLong(ligne, "utilisateurs_actifs_7j"))
                .alertesOuvertes(lireLong(ligne, "alertes_ouvertes"))
                .actionsHorsHoraire24h(lireLong(ligne, "actions_hors_horaire_24h"))
                .ipsMultiples24h(lireLong(ligne, "ips_multiples_24h"))
                .build();
    }

    // -------------------------------------------------------------------------
    // Utilitaires de lecture typée
    // -------------------------------------------------------------------------
    private long lireLong(Map<String, Object> ligne, String cle) {
        Object v = ligne.get(cle);
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }

    private BigDecimal lireBigDecimal(Map<String, Object> ligne, String cle) {
        Object v = ligne.get(cle);
        if (v == null) return null;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(v.toString());
    }
}
