// dossier/dto/response/PeineResponse.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.response;

import bf.gov.ascelc.logintegrite_backend.dossier.enums.NatureSanction;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.TypePeine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PeineResponse {
    private UUID id;
    private UUID implicationFaitId;
    private TypePeine typePeine;
    private NatureSanction natureSanction;
    private String duree;
    private BigDecimal montantAmende;
    private LocalDate dateDecision;
    private LocalDate dateExecution;
    private String description;
}
