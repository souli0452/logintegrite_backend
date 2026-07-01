package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID; // ── IMPORT INDISPENSABLE ──

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonnePhysiqueRequest {

    // Attributs spécifiques à l'individu
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 200, message = "Le prénom ne doit pas dépasser 200 caractères")
    private String prenoms;

    private LocalDate dateNaissance;

    @Size(max = 100, message = "Le lieu de naissance ne doit pas dépasser 100 caractères")
    private String lieuNaissance;

    @Size(max = 100, message = "La nationalité ne doit pas dépasser 100 caractères")
    private String nationalite;

    @Size(max = 50, message = "Le matricule ne doit pas dépasser 50 caractères")
    private String matricule;

    @Size(max = 200, message = "La fonction ne doit pas dépasser 200 caractères")
    private String fonction;

    @Size(max = 500, message = "L'URL de la photo ne doit pas dépasser 500 caractères")
    private String photoUrl;

    // ── CORRECTION : AJOUT DES CLÉS DE RÉFÉRENTIELS (UUID) ──
    @NotNull(message = "L'organisation (Entité) rattachement est obligatoire")
    private UUID entiteId;

    @NotNull(message = "La région de rattachement est obligatoire")
    private UUID regionId;
}