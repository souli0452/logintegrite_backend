package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutJudiciaireReferentielRequest {

    @NotBlank(message = "Le code du statut est obligatoire")
    @Size(max = 30, message = "Le code ne doit pas dépasser 30 caractères")
    private String code;

    @NotBlank(message = "Le libellé du statut est obligatoire")
    @Size(max = 150, message = "Le libellé ne doit pas dépasser 150 caractères")
    private String libelle;

    private String description;

    private Boolean actif;
}