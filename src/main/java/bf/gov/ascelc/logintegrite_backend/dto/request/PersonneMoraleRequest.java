package bf.gov.ascelc.logintegrite_backend.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class PersonneMoraleRequest {
    private String raisonSociale;
    private String sigle;
    private String ifu;
    private String typeStructure;
    private String nomResponsable;
    private String fonctionResponsable;

    // Liaisons relationnelles indispensables
    private UUID entiteId;
    private UUID regionId;
}