package bf.gov.ascelc.logintegrite_backend.parametresysteme.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParametreSystemeRequest {
    @NotBlank(message = "La cle est obligatoire")
    private String cle;
    private String valeur;
    private String description;
}
