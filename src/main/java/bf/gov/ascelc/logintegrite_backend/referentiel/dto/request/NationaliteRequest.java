package bf.gov.ascelc.logintegrite_backend.referentiel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NationaliteRequest {

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 150, message = "Le libellé ne peut dépasser 150 caractères")
    private String libelle;

    @Size(max = 3, message = "Le code ISO doit faire 3 caractères")
    private String codeIso;

    private Boolean actif = true;
}
