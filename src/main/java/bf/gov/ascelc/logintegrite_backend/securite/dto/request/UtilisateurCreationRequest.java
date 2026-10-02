package bf.gov.ascelc.logintegrite_backend.securite.dto.request;

import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UtilisateurCreationRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    private String telephone;

    @NotBlank(message = "Le mot de passe temporaire est obligatoire")
    @Size(min = 12, max = 128, message = "Le mot de passe doit avoir au moins 12 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "Le mot de passe doit contenir une majuscule, une minuscule et un chiffre")
    private String motDePasseTemporaire;

    /** Facultative, sauf pour un compte de consultation (6 mois par defaut). */
    private java.time.LocalDate dateExpiration;

    @NotNull(message = "Le role initial est obligatoire")
    private CodeRole roleInitial;
}
