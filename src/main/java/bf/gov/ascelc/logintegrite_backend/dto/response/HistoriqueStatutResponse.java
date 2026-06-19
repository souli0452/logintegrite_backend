package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HistoriqueStatutResponse extends AuditEntityDto {
    private UUID ficheId;
    private String ancientStatut;
    private String nouveauStatut;
    private String motif;
    private LocalDate dateJugement;
    private String juridiction;
    private String typePeine;
    private String dureePeine;
    private BigDecimal montantAmende;
    private String motifRelaxe;
}