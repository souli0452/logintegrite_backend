// dossier/dto/response/DossierAValiderResponse.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Vue agregee pour l'ecran validation : un dossier + les infos de la
// personne principale impliquee + la liste des faits en attente.
// Renvoye pour chaque dossier ayant au moins un fait EN_ATTENTE.
@Getter
@Builder
@AllArgsConstructor
public class DossierAValiderResponse {

    // Informations du dossier
    private UUID dossierId;
    private String numeroDossier;
    private String intitule;
    private LocalDate dateOuverture;
    private String sourceSignalementLibelle;

    // Informations de la personne principale impliquee
    // (premiere implication du dossier - la plus ancienne)
    private UUID personneId;
    private String personneNomAffichage;
    private String personneTypePersonne;    // "PHYSIQUE" ou "MORALE"
    private String personneRoleImplication; // "Auteur principal", "Complice"...

    // Faits en attente (uniquement statut EN_ATTENTE)
    private List<FaitReprocheResponse> faitsEnAttente;
    private int nombreFaitsEnAttente;
}
