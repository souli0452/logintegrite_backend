package bf.gov.ascelc.logintegrite_backend.dto.request;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiqueRequest {
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private UUID regionId;
    private UUID entiteId;
}