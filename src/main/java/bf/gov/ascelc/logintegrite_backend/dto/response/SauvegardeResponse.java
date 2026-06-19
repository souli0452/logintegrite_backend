package bf.gov.ascelc.logintegrite_backend.dto.response;

import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SauvegardeResponse {
    private UUID id;
    private String nomFichier;
    private String type;
    private String statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    // Attributs hérités de AuditEntity (Pour savoir quel admin a déclenché le backup manuel)
    private LocalDateTime createdAt;
    private String createdBy;
}