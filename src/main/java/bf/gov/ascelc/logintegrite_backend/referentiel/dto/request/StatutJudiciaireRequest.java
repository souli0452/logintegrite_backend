package bf.gov.ascelc.logintegrite_backend.referentiel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatutJudiciaireRequest {

    @NotBlank(message = "Le libelle est obligatoire")
    @Size(max = 100, message = "Le libelle ne peut pas depasser 100 caracteres")
    private String libelle;

    private boolean actif = true;
}
