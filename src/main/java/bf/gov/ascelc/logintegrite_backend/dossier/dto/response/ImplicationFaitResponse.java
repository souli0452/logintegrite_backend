// dossier/dto/response/ImplicationFaitResponse.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ImplicationFaitResponse {
    private UUID id;
    private UUID implicationId;
    private String personneNomAffichage;
    private UUID faitReprocheId;
    private String faitDescription;
    private BigDecimal faitMontantPrejudice;
    private UUID statutJudiciaireId;
    private String statutJudiciaireLibelle;
    private LocalDate dateStatut;
    private String commentaire;
}
