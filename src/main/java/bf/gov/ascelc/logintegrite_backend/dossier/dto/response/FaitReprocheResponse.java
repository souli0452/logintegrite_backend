// dossier/dto/response/FaitReprocheResponse.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.response;

import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class FaitReprocheResponse {
    private UUID id;
    private UUID dossierId;
    private UUID typeInfractionId;
    private String typeInfractionLibelle;
    private UUID zoneGeographiqueId;
    private String zoneGeographiqueLibelle;
    private LocalDate dateFaits;
    private String lieuPrecis;
    private String description;
    private BigDecimal montantPrejudice;
    private String devise;
    private BigDecimal montantConfirmeJustice;
    private StatutValidation statutValidation;
    private String motifRejet;
    private UUID validateParId;
    private String validateParNomComplet;
    private Instant dateValidation;
}
