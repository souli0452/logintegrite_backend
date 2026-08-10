package bf.gov.ascelc.logintegrite_backend.personne.dto.request;

import bf.gov.ascelc.logintegrite_backend.personne.enums.StatutPersonneMorale;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PersonneMoraleRequest {

    @NotBlank(message = "La dénomination sociale est obligatoire")
    private String denominationSociale;

    private String sigle;

    @NotBlank(message = "La forme juridique est obligatoire")
    private String formeJuridique;

    private String rccm;
    private String ifu;
    private BigDecimal capitalSocial;

    @NotBlank(message = "Le secteur d'activité est obligatoire")
    private String secteurActivite;

    @NotBlank(message = "Le siège social est obligatoire")
    private String siegeSocial;

    private String telephone;

    @Email(message = "Email invalide")
    private String email;

    private LocalDate dateCreationEntreprise;
    private StatutPersonneMorale statut = StatutPersonneMorale.ACTIVE;
    private UUID representantLegalId;
}
