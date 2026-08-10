// dossier/dto/request/ImplicationFaitRequest.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ImplicationFaitRequest {

    @NotNull(message = "Le fait reproche est obligatoire")
    private UUID faitReprocheId;

    @NotNull(message = "Le statut judiciaire est obligatoire")
    private UUID statutJudiciaireId;

    private String commentaire;
}
