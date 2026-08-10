package bf.gov.ascelc.logintegrite_backend.personne.dto.request;

import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePieceIdentite;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PieceIdentiteRequest {

    // ← CONSERVÉ pour rétrocompatibilité (accepté en entrée)
    private TypePieceIdentite typePiece;

    // 🆕 Nouveau champ prioritaire (utilisé par les formulaires modernes)
    private UUID typePieceId;

    @NotBlank(message = "Le numero est obligatoire")
    private String numero;

    private LocalDate dateDelivrance;
    private LocalDate dateExpiration;
}
