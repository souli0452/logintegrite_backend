package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.util.UUID; // Indispensable pour l'ID

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PersonnePhysiqueResponse extends FicheMiseEnCauseResponse {

    // L'identifiant unique de la fiche (si pas déjà inclus dans AuditEntityDto)
    private UUID id;

    private String nom;
    private String prenoms;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String nationalite;
    private String matricule;
    private String fonction;
    private String photoUrl;

    // ── AJOUT CRITIQUE : Cycle de vie et suivi de la fiche ──
    private String statutFiche;       // BROUILLON, EN_ATTENTE_VALIDATION, ACTIVE, REJETE, ARCHIVE
    private String statutJudiciaire;  // Situation judiciaire de la personne
    private String motifRejet;        // Renseigné si le validateur clique sur "Rejeter"

    // ── AJOUT CRITIQUE : Objets de référentiels imbriqués ──
    // Permet à Angular d'écrire directement : personne.region.libelle ou personne.entite.nom
    private RegionResponse region;
    private EntiteOrganisationResponse entite;
}