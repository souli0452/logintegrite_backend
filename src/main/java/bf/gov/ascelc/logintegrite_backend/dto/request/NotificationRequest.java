package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotBlank(message = "L'ID du destinataire est obligatoire")
    @Size(max = 100, message = "L'ID du destinataire ne doit pas dépasser 100 caractères")
    private String destinataireId;

    @Size(max = 50, message = "Le type de notification ne doit pas dépasser 50 caractères")
    private String type;

    @NotBlank(message = "Le contenu de la notification est obligatoire")
    private String contenu;
}