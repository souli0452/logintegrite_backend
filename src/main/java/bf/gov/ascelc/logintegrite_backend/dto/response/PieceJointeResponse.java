package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PieceJointeResponse extends AuditEntityDto {
    private UUID ficheId;
    private UUID infractionId;
    private String nomFichier;
    private String typeFichier;
    private Long tailleOctets;
    private String urlStockage;
}