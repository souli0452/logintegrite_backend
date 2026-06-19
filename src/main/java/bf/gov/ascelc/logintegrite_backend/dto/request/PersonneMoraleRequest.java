package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonneMoraleRequest {

    @NotBlank(message = "La raison sociale est obligatoire")
    @Size(max = 300, message = "La raison sociale ne doit pas dépasser 300 caractères")
    private String raisonSociale;

    @Size(max = 50, message = "Le sigle ne doit pas dépasser 50 caractères")
    private String sigle;

    @Size(max = 50, message = "Le numéro IFU ne doit pas dépasser 50 caractères")
    private String ifu;

    @Size(max = 50, message = "Le type de structure ne doit pas dépasser 50 caractères")
    private String typeStructure;

    @Size(max = 200, message = "Le nom du responsable ne doit pas dépasser 200 caractères")
    private String nomResponsable;

    @Size(max = 200, message = "La fonction du responsable ne doit pas dépasser 200 caractères")
    private String fonctionResponsable;
}