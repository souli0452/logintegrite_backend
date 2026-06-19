package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EntiteOrganisationResponse extends AuditEntityDto {


    private String code;
    private String nom;
    private String type;
    private Boolean actif;


}