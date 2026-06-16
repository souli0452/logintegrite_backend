package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class PersonnePhysiqueRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenoms;

    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String nationalite;
    private String matricule;
    private String photoUrl;
    private String fonction;

    // Liaisons relationnelles indispensables
    private UUID entiteId;
    private UUID regionId;
}