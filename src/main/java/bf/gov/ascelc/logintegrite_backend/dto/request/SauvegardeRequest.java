package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SauvegardeRequest {

    @NotBlank(message = "Le nom du fichier de sauvegarde est obligatoire")
    private String nomFichier;

    @NotBlank(message = "Le type de sauvegarde (AUTO ou MANUEL) est obligatoire")
    private String type;

    @NotBlank(message = "Le statut de la sauvegarde est obligatoire")
    private String statut;

    @NotNull(message = "La date et l'heure de début sont obligatoires")
    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;
}