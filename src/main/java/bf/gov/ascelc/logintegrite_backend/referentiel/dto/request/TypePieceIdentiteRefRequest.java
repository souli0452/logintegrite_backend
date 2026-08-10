package bf.gov.ascelc.logintegrite_backend.referentiel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypePieceIdentiteRefRequest {

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 30, message = "Le code ne peut dépasser 30 caractères")
    @Pattern(regexp = "^[A-Z_]+$",
             message = "Le code doit contenir uniquement des lettres majuscules et des underscores")
    private String code;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 150, message = "Le libellé ne peut dépasser 150 caractères")
    private String libelle;

    private Boolean actif = true;
}
