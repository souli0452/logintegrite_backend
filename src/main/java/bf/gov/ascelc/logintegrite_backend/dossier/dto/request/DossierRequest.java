// dossier/dto/request/DossierRequest.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DossierRequest {

    private String numeroDossier;
    private String intitule;

    @NotNull(message = "La source de signalement est obligatoire")
    private UUID sourceSignalementId;

    @Size(max = 5000, message = "La description ne doit pas depasser 5000 caracteres")
private String descriptionContexte;
}
