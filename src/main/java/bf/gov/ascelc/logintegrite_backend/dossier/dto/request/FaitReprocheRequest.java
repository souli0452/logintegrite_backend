// dossier/dto/request/FaitReprocheRequest.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class FaitReprocheRequest {

    @NotNull(message = "Le type d'infraction est obligatoire")
    private UUID typeInfractionId;

    private UUID zoneGeographiqueId;

    @NotNull(message = "La date des faits est obligatoire")
    @PastOrPresent(message = "La date des faits ne peut pas etre dans le futur")
    private LocalDate dateFaits;

    private String lieuPrecis;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "Le montant du prejudice est obligatoire")
    @PositiveOrZero(message = "Le montant doit etre positif ou nul")
    private BigDecimal montantPrejudice;

    private String devise = "XOF";

    private BigDecimal montantConfirmeJustice;
}
