// dossier/dto/request/PeineRequest.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.request;

import bf.gov.ascelc.logintegrite_backend.dossier.enums.NatureSanction;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.TypePeine;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PeineRequest {

    @NotNull(message = "Le type de peine est obligatoire")
    private TypePeine typePeine;

    private NatureSanction natureSanction;
    private String duree;

    @PositiveOrZero(message = "Le montant doit etre positif ou nul")
    private BigDecimal montantAmende;

    private LocalDate dateDecision;
    private LocalDate dateExecution;
    private String description;
}
