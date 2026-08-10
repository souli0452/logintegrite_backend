// dossier/dto/request/AjouterDossierPersonneRequest.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AjouterDossierPersonneRequest {

    // Bloc 1 : informations du dossier
    @NotNull(message = "Les informations du dossier sont obligatoires")
    @Valid
    private DossierRequest dossier;

    // Bloc 2 : implication de la personne dans ce dossier
    @NotNull(message = "Le role de la personne dans ce dossier est obligatoire")
    private UUID roleImplicationId;

    private UUID entiteOrganisationId;
    private String fonctionOccupee;

    // Nouveaux champs statut judiciaire porte par l'implication
    private UUID statutJudiciaireId;
    private String autoriteCompetente;
    private String referenceAffaire;

    // Bloc 3 : au moins un fait reproche
    @NotEmpty(message = "Au moins un fait reproche est obligatoire")
    @Valid
    private List<FaitReprocheRequest> faits;
}
