package bf.gov.ascelc.logintegrite_backend.personne.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AliasRequest {
    @NotBlank(message = "Le nom d'alias est obligatoire")
    private String nomAlias;
    private String commentaire;
}
