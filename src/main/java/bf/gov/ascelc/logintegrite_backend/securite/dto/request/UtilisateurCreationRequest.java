package bf.gov.ascelc.logintegrite_backend.securite.dto.request;

import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Size(min = 8, message = "Le mot de passe doit avoir au moins 8 caracteres")
    private String motDePasseTemporaire;

    @NotNull(message = "Le role initial est obligatoire")
    private CodeRole roleInitial;
}
