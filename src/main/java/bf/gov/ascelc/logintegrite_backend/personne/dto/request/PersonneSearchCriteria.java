package bf.gov.ascelc.logintegrite_backend.personne.dto.request;

import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePersonne;
import bf.gov.ascelc.logintegrite_backend.personne.enums.StatutAncrage;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PersonneSearchCriteria {

    // Criteres d'identite 
    private String nomOuDenomination;
    private TypePersonne typePersonne;
    private String nationalite;
    private String numeroPieceIdentite;
    private String rccm;
    private String ifu;
    private StatutAncrage statutAncrage;

    // Recherche avancee : infraction, entite, region, periode, statut judiciaire
    private UUID typeInfractionId;
    private UUID zoneGeographiqueId;
    private UUID statutJudiciaireId;
    private UUID entiteOrganisationId;
    private String fonction;
    private LocalDate periodeDebut;
    private LocalDate periodeFin;
}
