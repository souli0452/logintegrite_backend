package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionRequest {

    @NotBlank(message = "Le code de la région est obligatoire")
    @Size(max = 10, message = "Le code ne doit pas dépasser 10 caractères")
    private String code;

    @NotBlank(message = "Le nom de la région est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    private Boolean actif;
}