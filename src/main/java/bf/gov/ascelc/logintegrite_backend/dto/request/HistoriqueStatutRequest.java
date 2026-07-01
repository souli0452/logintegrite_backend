package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueStatutRequest {

    @NotNull(message = "L'ID de la fiche associée est obligatoire")
    private UUID ficheId;

    // AJOUT : optionnel — précise quelle infraction de la fiche est concernée
    private UUID infractionId;

    @Size(max = 50, message = "L'ancien statut ne doit pas dépasser 50 caractères")
    private String ancientStatut;

    @NotBlank(message = "Le nouveau statut est obligatoire")
    @Size(max = 50, message = "Le nouveau statut ne doit pas dépasser 50 caractères")
    private String nouveauStatut;

    private String motif;

    private LocalDate dateJugement;

    @Size(max = 200, message = "La juridiction ne doit pas dépasser 200 caractères")
    private String juridiction;

    @Size(max = 200, message = "Le type de peine ne doit pas dépasser 200 caractères")
    private String typePeine;

    @Size(max = 100, message = "La durée de la peine ne doit pas dépasser 100 caractères")
    private String dureePeine;

    private BigDecimal montantAmende;

    private String motifRelaxe;
}