package bf.gov.ascelc.logintegrite_backend.dto.response;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferentielResponse {
    private UUID id;
    private String libelle; // Contiendra le nom de la région, de l'entité ou de l'infraction
}