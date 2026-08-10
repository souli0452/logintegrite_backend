// securite/dto/request/RoleHabilitationRequest.java
package bf.gov.ascelc.logintegrite_backend.securite.dto.request;

import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleHabilitationRequest {

    @NotNull(message = "Le code est obligatoire")
    private CodeRole code;

    @NotBlank(message = "Le libelle est obligatoire")
    private String libelle;

    private boolean accesVueGlobaleDossier = false;
}
