package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PersonneMoraleResponse extends FicheMiseEnCauseResponse {

    // Identifiant unique de la fiche (aligné sur le DTO Physique)
    private UUID id;

    private String raisonSociale;
    private String sigle;
    private String ifu;
    private String typeStructure;
    private String nomResponsable;
    private String fonctionResponsable;

    // ── ALIGNEMENT LOGIQUE : Cycle de vie et suivi de la fiche ──
    private String statutFiche;       // BROUILLON, EN_ATTENTE_VALIDATION, ACTIVE, REJETE, ARCHIVE
    private String statutJudiciaire;  // Situation judiciaire de la structure
    private String motifRejet;        // Renseigné si le validateur clique sur "Rejeter"

    // ── ALIGNEMENT STRUCTUREL : Objets de référentiels imbriqués ──
    private RegionResponse region;
    private EntiteOrganisationResponse entite;
}