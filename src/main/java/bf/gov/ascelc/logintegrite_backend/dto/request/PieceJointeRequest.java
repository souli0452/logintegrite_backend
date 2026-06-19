package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceJointeRequest {

    @NotNull(message = "L'ID de la fiche de mise en cause associée est obligatoire")
    private UUID ficheId;

    @NotBlank(message = "Le nom du fichier est obligatoire")
    @Size(max = 255, message = "Le nom du fichier ne doit pas dépasser 255 caractères")
    private String nomFichier;

    @Size(max = 50, message = "Le type de fichier ne doit pas dépasser 50 caractères")
    private String typeFichier;

    private Long tailleOctets;

    @NotBlank(message = "L'URL de stockage est obligatoire")
    @Size(max = 500, message = "L'URL de stockage ne doit pas dépasser 500 caractères")
    private String urlStockage;
}