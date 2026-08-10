package bf.gov.ascelc.logintegrite_backend.referentiel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TypeInfractionRequest {

    @NotNull(message = "La categorie d'infraction est obligatoire")
    private UUID categorieInfractionId;

    @NotBlank(message = "Le libelle est obligatoire")
    private String libelle;

    private boolean actif = true;
}
