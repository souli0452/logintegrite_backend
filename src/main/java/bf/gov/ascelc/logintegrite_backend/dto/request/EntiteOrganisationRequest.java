package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntiteOrganisationRequest {

    @NotBlank(message = "Le code de l'organisation est obligatoire")
    @Size(max = 20, message = "Le code ne doit pas dépasser 20 caractères")
    private String code;

    @NotBlank(message = "Le nom de l'organisation est obligatoire")
    @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
    private String nom;

    @Size(max = 50, message = "Le type ne doit pas dépasser 50 caractères")
    private String type;

    private Boolean actif;
}