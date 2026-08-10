package bf.gov.ascelc.logintegrite_backend.personne.dto.response;

import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePieceIdentite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PieceIdentiteResponse {

    private UUID id;

    // ← CONSERVÉ
    private TypePieceIdentite typePiece;

    // 🆕 Nouveaux champs pour l'affichage
    private UUID typePieceId;
    private String typePieceCode;      // code du référentiel
    private String typePieceLibelle;   // libellé lisible pour affichage

    private String numero;
    private LocalDate dateDelivrance;
    private LocalDate dateExpiration;
}
