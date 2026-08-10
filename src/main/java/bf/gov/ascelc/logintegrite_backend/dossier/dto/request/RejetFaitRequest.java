// dossier/dto/request/RejetFaitRequest.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejetFaitRequest {

    @NotBlank(message = "Le motif de rejet est obligatoire")
    private String motifRejet;
}
