package bf.gov.ascelc.logintegrite_backend.dossier.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ImplicationRequest {

    @NotNull(message = "La personne est obligatoire")
    private UUID personneId;

    @NotNull(message = "Le role est obligatoire")
    private UUID roleImplicationId;

    private UUID entiteOrganisationId;
    private UUID statutJudiciaireId;
    private String autoriteCompetente;
    private String referenceAffaire;
    private String fonctionOccupee;
    private String entiteLibelleALEpoque;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String observations;
}
