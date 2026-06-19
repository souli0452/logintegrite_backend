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
public class FicheExportResponse extends AuditEntityDto {
    private String typeFiche;
    private String cibleNom;
    private String identifiantUnique;
    private String structureAssociee;
    private String region;
    private String statutFiche;
    private String statutJudiciaire;
    private Integer nbInfractions;
}