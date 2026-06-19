package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechercheSauvegardeeRequest {

    @NotBlank(message = "Le nom de la recherche sauvegardée est obligatoire")
    @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
    private String nom;

    @NotBlank(message = "Les critères de recherche sous format JSON sont obligatoires")
    private String criteres;
}