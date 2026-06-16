package bf.gov.ascelc.logintegrite_backend.dto.request;

import lombok.Data;

@Data
public class InfractionRequest {
    private String nature; // Changement de type vers String
    private String description;
}