package bf.gov.ascelc.logintegrite_backend.statistiques.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DashboardExecutifResponse {

    // --- Cartes chiffrees du haut ---
    private long totalDossiers;
    private long totalDossiersValides;      // dossiers dont tous les faits sont VALIDEE
    private long totalDossiersEnAttente;    // dossiers avec au moins un fait EN_ATTENTE
    private long totalDossiersRejetes;      // dossiers dont au moins un fait est REJETEE
    private long totalPersonnesImpliquees;  // count(DISTINCT personne_id) dans implication

    // --- Deltas vs annee precedente (calcules cote backend) ---
    private Double deltaDossiers;
    private Double deltaValides;
    private Double deltaEnAttente;
    private Double deltaRejetes;
    private Double deltaPersonnes;

    // --- Evolution 5 dernieres annees (dossiers ouverts par an) ---
    private List<PointEvolution> evolutionDossiers;

    // --- Repartitions ---
    private List<RepartitionItem> parCategorieInfraction;
    private List<RepartitionItem> parRegion;
    private List<RepartitionItem> parStatutDossier;

    // --- Top structures ---
    private List<TopStructureItem> topStructures;

    // --- Metriques operationnelles ---
    private Double delaiMoyenValidationJours;   // moyenne (dateValidation - dateCreation) sur FaitReproche VALIDEE
    private Double tauxCompletudeGlobal;        // % de faits VALIDEE / total faits (100 si aucun fait)

    // --- Activites recentes ---
    private List<ActiviteRecenteItem> activitesRecentes;

    @Getter @AllArgsConstructor
    public static class PointEvolution {
        private int annee;
        private long valeur;
    }

    @Getter @AllArgsConstructor
    public static class RepartitionItem {
        private String libelle;
        private long valeur;
        private double pourcentage;
    }

    @Getter @AllArgsConstructor
    public static class TopStructureItem {
        private int rang;
        private String libelle;
        private long nombre;
    }

    @Getter @AllArgsConstructor
    public static class ActiviteRecenteItem {
        private String type;         // "DOSSIER_CREE", "FAIT_VALIDE", "PERSONNE_AJOUTEE"...
        private String description;
        private String dateAction;   // ISO instant
    }
}
