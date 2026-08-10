package bf.gov.ascelc.logintegrite_backend.referentiel.dto.request;

import bf.gov.ascelc.logintegrite_backend.referentiel.enums.NiveauEntite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EntiteOrganisationRequest {

    @NotBlank(message = "Le libelle est obligatoire")
    @Size(max = 200, message = "Le libelle ne peut pas depasser 200 caracteres")
    private String libelle;

    @NotNull(message = "Le niveau est obligatoire")
    private NiveauEntite niveau;

    // Optionnel : parent hierarchique (une direction rattachee a un ministere, etc.)
    private UUID parentId;
}
