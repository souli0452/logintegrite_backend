package bf.gov.ascelc.logintegrite_backend.parametresysteme.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ParametreSystemeResponse {
    private UUID id;
    private String cle;
    private String valeur;
    private String description;
}
