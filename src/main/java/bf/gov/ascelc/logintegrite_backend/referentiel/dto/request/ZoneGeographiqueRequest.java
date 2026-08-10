package bf.gov.ascelc.logintegrite_backend.referentiel.dto.request;

import bf.gov.ascelc.logintegrite_backend.referentiel.enums.NiveauZone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ZoneGeographiqueRequest {

    @NotBlank(message = "Le libelle est obligatoire")
    @Size(max = 150, message = "Le libelle ne peut pas depasser 150 caracteres")
    private String libelle;

    @NotNull(message = "Le niveau est obligatoire")
    private NiveauZone niveau;

    // Optionnel : parent hierarchique (une commune a une province, etc.)
    private UUID parentId;

    @Size(max = 20, message = "Le code ne peut pas depasser 20 caracteres")
    private String code;
}
