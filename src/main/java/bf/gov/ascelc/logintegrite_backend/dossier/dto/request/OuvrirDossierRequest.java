// dossier/dto/request/OuvrirDossierRequest.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.request;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonnePhysiqueRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OuvrirDossierRequest {

    private UUID personneExistanteId;
    @Valid
    private PersonnePhysiqueRequest nouvellePersonnePhysique;
    @Valid
    private PersonneMoraleRequest nouvellePersonneMorale;

    @NotNull(message = "Les informations du dossier sont obligatoires")
    @Valid
    private DossierRequest dossier;

    @NotNull(message = "Le role de la personne dans ce dossier est obligatoire")
    private UUID roleImplicationId;
    private UUID entiteOrganisationId;
    private String fonctionOccupee;

    @NotNull(message = "Le premier fait reproche est obligatoire pour ouvrir un dossier")
    @Valid
    private FaitReprocheRequest premierFait;
}
