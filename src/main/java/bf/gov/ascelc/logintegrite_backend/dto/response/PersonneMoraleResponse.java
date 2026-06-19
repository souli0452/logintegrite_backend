package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PersonneMoraleResponse extends AuditEntityDto {
    private String raisonSociale;
    private String sigle;
    private String ifu;
    private String typeStructure;
    private String nomResponsable;
    private String fonctionResponsable;
}