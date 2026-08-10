package bf.gov.ascelc.logintegrite_backend.personne.dto.response;

import bf.gov.ascelc.logintegrite_backend.personne.enums.StatutPersonneMorale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PersonneMoraleResponse {
    private UUID id;
    private String nomAffichage;
    private String denominationSociale;
    private String sigle;
    private String formeJuridique;
    private String rccm;
    private String ifu;
    private BigDecimal capitalSocial;
    private String secteurActivite;
    private String siegeSocial;
    private String telephone;
    private String email;
    private Instant dateCreation;
    private String creeParNomComplet;
    private boolean aUnLogo;
    private LocalDate dateCreationEntreprise;
    private StatutPersonneMorale statut;
    private UUID representantLegalId;
    private String representantLegalNomComplet;
}
