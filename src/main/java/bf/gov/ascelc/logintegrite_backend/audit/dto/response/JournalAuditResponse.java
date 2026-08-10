package bf.gov.ascelc.logintegrite_backend.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class JournalAuditResponse {
    private UUID id;
    private String action;
    private String entiteCible;
    private UUID entiteCibleId;
    private Object valeurAvant;
    private Object valeurApres;
    private String hashPrecedent;
    private String hashActuel;
    private Instant dateAction;
    private String adresseIp;
    private UUID utilisateurId;
    private String utilisateurNomComplet;
}
