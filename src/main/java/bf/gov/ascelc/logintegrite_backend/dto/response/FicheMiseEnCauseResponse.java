package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class FicheMiseEnCauseResponse extends AuditEntityDto {
    private String typeFiche;
    private String cibleNom;
    private String identifiantUnique;
    private String entiteNom;
    private String regionNom;
    private String statutFiche;
    private String statutJudiciaire;
    private LocalDateTime dateModification;
    private String photoUrl;                      
    private String natureInfractionPrincipale;    
    private Integer nombreInfractions; 
}
