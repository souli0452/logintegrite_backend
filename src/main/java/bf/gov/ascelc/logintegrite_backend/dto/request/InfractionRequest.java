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
public class InfractionRequest {

    @NotNull(message = "L'ID de la fiche de mise en cause est obligatoire")
    private UUID ficheId;

    @NotBlank(message = "La nature de l'infraction est obligatoire")
    @Size(max = 30, message = "La nature ne doit pas dépasser 30 caractères")
    private String nature;

    private UUID typeInfractionId; // Optionnel selon ton modèle, mais recommandé si qualifié

    private String description;

    @NotNull(message = "La date des faits est obligatoire")
    private LocalDate dateFaits;

    @Size(max = 200, message = "Le lieu des faits ne doit pas dépasser 200 caractères")
    private String lieuFaits;

    private BigDecimal montant;

    @Size(max = 10, message = "La devise ne doit pas dépasser 10 caractères")
    private String devise;

    private String sources;
}