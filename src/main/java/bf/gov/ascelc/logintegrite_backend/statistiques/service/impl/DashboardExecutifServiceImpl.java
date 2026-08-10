package bf.gov.ascelc.logintegrite_backend.statistiques.service.impl;

import bf.gov.ascelc.logintegrite_backend.statistiques.dto.response.DashboardExecutifResponse;
import bf.gov.ascelc.logintegrite_backend.statistiques.dto.response.DashboardExecutifResponse.*;
import bf.gov.ascelc.logintegrite_backend.statistiques.service.DashboardExecutifService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardExecutifServiceImpl implements DashboardExecutifService {

    private final JdbcTemplate jdbc;

    @Override
    @Transactional(readOnly = true)
    public DashboardExecutifResponse calculer() {
        int anneeCourante = Year.now().getValue();
        int anneePrecedente = anneeCourante - 1;

        // --- Cartes chiffrees ---
        long totalDossiers = countLong("SELECT COUNT(*) FROM dossiers.dossier");
        long totalDossiersValides = countLong(
            "SELECT COUNT(DISTINCT d.id) FROM dossiers.dossier d " +
            "WHERE d.id NOT IN (SELECT dossier_id FROM dossiers.fait_reproche " +
            "                    WHERE statut_validation != 'VALIDEE') " +
            "AND EXISTS (SELECT 1 FROM dossiers.fait_reproche WHERE dossier_id = d.id)"
        );
        long totalDossiersEnAttente = countLong(
            "SELECT COUNT(DISTINCT dossier_id) FROM dossiers.fait_reproche " +
            "WHERE statut_validation = 'EN_ATTENTE'"
        );
        long totalDossiersRejetes = countLong(
            "SELECT COUNT(DISTINCT dossier_id) FROM dossiers.fait_reproche " +
            "WHERE statut_validation = 'REJETEE'"
        );
        long totalPersonnesImpliquees = countLong(
            "SELECT COUNT(DISTINCT personne_id) FROM dossiers.implication"
        );

        // --- Deltas vs annee precedente ---
        long dossiersAnneeCourante = countLong(
            "SELECT COUNT(*) FROM dossiers.dossier WHERE EXTRACT(YEAR FROM date_ouverture) = ?",
            anneeCourante
        );
        long dossiersAnneePrecedente = countLong(
            "SELECT COUNT(*) FROM dossiers.dossier WHERE EXTRACT(YEAR FROM date_ouverture) = ?",
            anneePrecedente
        );

        long personnesAnneeCourante = countLong(
            "SELECT COUNT(DISTINCT p.id) FROM personnes.personne p " +
            "WHERE EXTRACT(YEAR FROM p.date_creation) = ?",
            anneeCourante
        );
        long personnesAnneePrecedente = countLong(
            "SELECT COUNT(DISTINCT p.id) FROM personnes.personne p " +
            "WHERE EXTRACT(YEAR FROM p.date_creation) = ?",
            anneePrecedente
        );

        // --- Evolution 5 ans ---
        List<PointEvolution> evolution = new ArrayList<>();
        for (int an = anneeCourante - 4; an <= anneeCourante; an++) {
            long val = countLong(
                "SELECT COUNT(*) FROM dossiers.dossier " +
                "WHERE EXTRACT(YEAR FROM date_ouverture) = ?",
                an
            );
            evolution.add(new PointEvolution(an, val));
        }

        // --- Repartitions ---
        List<RepartitionItem> parCategorie = repartitionParCategorie();
        List<RepartitionItem> parRegion = repartitionParRegion();
        List<RepartitionItem> parStatut = repartitionParStatutDossier(totalDossiers);

        // --- Top structures (entites d'organisation les plus impliquees) ---
        List<TopStructureItem> topStructures = topStructures();

        // --- Metriques operationnelles ---
        Double delaiMoyen = jdbc.queryForObject(
            "SELECT AVG(EXTRACT(EPOCH FROM (date_validation - date_creation)) / 86400.0) " +
            "FROM dossiers.fait_reproche " +
            "WHERE statut_validation = 'VALIDEE' AND date_validation IS NOT NULL",
            Double.class
        );
        if (delaiMoyen == null) delaiMoyen = 0.0;

        Long totalFaits = jdbc.queryForObject(
            "SELECT COUNT(*) FROM dossiers.fait_reproche", Long.class
        );
        Long faitsValides = jdbc.queryForObject(
            "SELECT COUNT(*) FROM dossiers.fait_reproche WHERE statut_validation = 'VALIDEE'",
            Long.class
        );
        double tauxCompletude = (totalFaits == null || totalFaits == 0)
            ? 100.0
            : ((faitsValides == null ? 0 : faitsValides) * 100.0) / totalFaits;

        // --- Activites recentes ---
        List<ActiviteRecenteItem> activitesRecentes = activitesRecentes();

        return DashboardExecutifResponse.builder()
            .totalDossiers(totalDossiers)
            .totalDossiersValides(totalDossiersValides)
            .totalDossiersEnAttente(totalDossiersEnAttente)
            .totalDossiersRejetes(totalDossiersRejetes)
            .totalPersonnesImpliquees(totalPersonnesImpliquees)
            .deltaDossiers(calculerDelta(dossiersAnneeCourante, dossiersAnneePrecedente))
            .deltaValides(null)     // pas de comparaison sensee sans historique VALIDEE
            .deltaEnAttente(null)
            .deltaRejetes(null)
            .deltaPersonnes(calculerDelta(personnesAnneeCourante, personnesAnneePrecedente))
            .evolutionDossiers(evolution)
            .parCategorieInfraction(parCategorie)
            .parRegion(parRegion)
            .parStatutDossier(parStatut)
            .topStructures(topStructures)
            .delaiMoyenValidationJours(Math.round(delaiMoyen * 10.0) / 10.0)
            .tauxCompletudeGlobal(Math.round(tauxCompletude * 10.0) / 10.0)
            .activitesRecentes(activitesRecentes)
            .build();
    }

    private long countLong(String sql, Object... args) {
        Long result = jdbc.queryForObject(sql, Long.class, args);
        return result == null ? 0L : result;
    }

    // Retourne null si base de comparaison = 0 (delta indefini)
    private Double calculerDelta(long courant, long precedent) {
        if (precedent == 0) return null;
        double delta = ((courant - precedent) * 100.0) / precedent;
        return Math.round(delta * 10.0) / 10.0;
    }

    private List<RepartitionItem> repartitionParCategorie() {
        List<Object[]> rows = jdbc.query(
            "SELECT ci.libelle, COUNT(fr.id) as nb " +
            "FROM dossiers.fait_reproche fr " +
            "JOIN referentiels.type_infraction ti ON ti.id = fr.type_infraction_id " +
            "JOIN referentiels.categorie_infraction ci ON ci.id = ti.categorie_infraction_id " +
            "GROUP BY ci.libelle " +
            "ORDER BY nb DESC " +
            "LIMIT 8",
            (rs, i) -> new Object[]{ rs.getString(1), rs.getLong(2) }
        );
        long total = rows.stream().mapToLong(r -> (Long) r[1]).sum();
        return rows.stream()
            .map(r -> new RepartitionItem(
                (String) r[0],
                (Long) r[1],
                total == 0 ? 0 : Math.round(((Long) r[1] * 100.0 / total) * 10.0) / 10.0
            ))
            .toList();
    }

    private List<RepartitionItem> repartitionParRegion() {
    List<Object[]> rows = jdbc.query(
        "SELECT zg.libelle, COUNT(fr.id) as nb " +
        "FROM dossiers.fait_reproche fr " +
        "JOIN referentiels.zone_geographique zg ON zg.id = fr.zone_geographique_id " +
        "GROUP BY zg.libelle " +
        "ORDER BY nb DESC " +
        "LIMIT 8",
        (rs, i) -> new Object[]{ rs.getString(1), rs.getLong(2) }
    );
    long total = rows.stream().mapToLong(r -> (Long) r[1]).sum();
    return rows.stream()
        .map(r -> new RepartitionItem(
            (String) r[0],
            (Long) r[1],
            total == 0 ? 0 : Math.round(((Long) r[1] * 100.0 / total) * 10.0) / 10.0
        ))
        .toList();
}

    private List<RepartitionItem> repartitionParStatutDossier(long totalDossiers) {
        long ouverts = countLong("SELECT COUNT(*) FROM dossiers.dossier WHERE statut_dossier = 'OUVERT'");
        long clotures = countLong("SELECT COUNT(*) FROM dossiers.dossier WHERE statut_dossier = 'CLOTURE'");

        List<RepartitionItem> items = new ArrayList<>();
        if (totalDossiers > 0) {
            items.add(new RepartitionItem("Ouverts", ouverts, Math.round((ouverts * 100.0 / totalDossiers) * 10.0) / 10.0));
            items.add(new RepartitionItem("Clotures", clotures, Math.round((clotures * 100.0 / totalDossiers) * 10.0) / 10.0));
        }
        return items;
    }

    private List<TopStructureItem> topStructures() {
        List<Object[]> rows = jdbc.query(
            "SELECT eo.libelle, COUNT(DISTINCT i.dossier_id) as nb " +
            "FROM dossiers.implication i " +
            "JOIN referentiels.entite_organisation eo ON eo.id = i.entite_organisation_id " +
            "GROUP BY eo.libelle " +
            "ORDER BY nb DESC " +
            "LIMIT 5",
            (rs, i) -> new Object[]{ rs.getString(1), rs.getLong(2) }
        );
        List<TopStructureItem> items = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            items.add(new TopStructureItem(i + 1, (String) rows.get(i)[0], (Long) rows.get(i)[1]));
        }
        return items;
    }

    private List<ActiviteRecenteItem> activitesRecentes() {
        // On lit dans journal_audit les 10 dernieres modifications
        return jdbc.query(
            "SELECT action, entite_cible, date_action " +
            "FROM audit.journal_audit " +
            "ORDER BY date_action DESC " +
            "LIMIT 10",
            (rs, i) -> new ActiviteRecenteItem(
                rs.getString("action") + "_" + rs.getString("entite_cible"),
                describeAction(rs.getString("action"), rs.getString("entite_cible")),
                rs.getTimestamp("date_action").toInstant().toString()
            )
        );
    }

    private String describeAction(String action, String entite) {
        String verbe = switch (action) {
            case "INSERT" -> "cree";
            case "UPDATE" -> "modifie";
            case "DELETE" -> "supprime";
            default -> "traite";
        };
        return entite + " " + verbe;
    }
}
