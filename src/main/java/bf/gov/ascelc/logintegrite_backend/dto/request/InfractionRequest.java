// dto/request/InfractionRequest.java
package bf.gov.ascelc.logintegrite_backend.dto.request;

import bf.gov.ascelc.logintegrite_backend.entity.Infraction.NatureInfraction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class InfractionRequest {

    @NotNull
    private NatureInfraction nature;

    @NotNull
    private LocalDate dateFaits;

    private String description;
    private String lieuFaits;
    private Double montant;
    private String devise;
    private Long typeInfractionId;
    private String sources;
}
