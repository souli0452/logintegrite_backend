// dossier/dto/request/MiseAJourStatutJudiciaireRequest.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class MiseAJourStatutJudiciaireRequest {

    @NotNull(message = "Le statut judiciaire est obligatoire")
    private UUID statutJudiciaireId;

    private LocalDate dateStatut;

    private String motif;

    private String autoriteCompetente;

    private String referenceAffaire;
}
