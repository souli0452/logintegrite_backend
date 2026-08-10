// dossier/dto/response/FaitRejeteResponse.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class FaitRejeteResponse {
    private UUID id;

    // Contexte dossier
    private UUID dossierId;
    private String numeroDossier;
    private String intitule;

    // Contexte personne (via la premiere implication du dossier)
    private UUID personneId;
    private String personneNomAffichage;
    private String personneTypePersonne;   // "PHYSIQUE" ou "MORALE"

    // Fait
    private String typeInfractionLibelle;
    private LocalDate dateFaits;
    private String description;
    private BigDecimal montantPrejudice;
    private String devise;

    // Info du rejet
    private String motifRejet;
    private Instant dateRejet;
    private String rejeteParNomComplet;
}
