// dossier/dto/response/ImplicationResponse.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ImplicationResponse {
    private UUID id;
    private UUID dossierId;
    private UUID personneId;
    private String personneNomAffichage;
    private UUID roleImplicationId;
    private String roleImplicationLibelle;
    private UUID entiteOrganisationId;
    private String entiteOrganisationLibelle;
    private String fonctionOccupee;
    private String entiteLibelleALEpoque;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String observations;
    private UUID statutJudiciaireId;
private String statutJudiciaireLibelle;
private String autoriteCompetente;
private String referenceAffaire;
}
