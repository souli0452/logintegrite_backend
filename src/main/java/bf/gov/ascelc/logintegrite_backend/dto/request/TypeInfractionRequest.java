package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeInfractionRequest {

    @NotBlank(message = "Le code du type d'infraction est obligatoire")
    @Size(max = 20, message = "Le code ne doit pas dépasser 20 caractères")
    private String code;

    @NotBlank(message = "Le libellé du type d'infraction est obligatoire")
    @Size(max = 100, message = "Le libellé ne doit pas dépasser 100 caractères")
    private String libelle;

    private String description;
}