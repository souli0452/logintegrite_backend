package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InfractionResponse extends AuditEntityDto {



    private UUID ficheId;
    private String nature;

    // Jointures aplaties pour faciliter l'affichage immédiat dans l'IHM du front
    private UUID typeInfractionId;
    private String typeInfractionLibelle;
    private String typeInfractionCode;

    private String description;
    private LocalDate dateFaits;
    private String lieuFaits;
    private BigDecimal montant;
    private String devise;
    private String sources;

}