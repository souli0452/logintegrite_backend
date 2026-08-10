package bf.gov.ascelc.logintegrite_backend.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class JournalConsultationResponse {
    private UUID id;
    private String entiteConsultee;
    private UUID entiteConsulteeId;
    private Instant dateConsultation;
    private String adresseIp;
    private UUID utilisateurId;
    private String utilisateurNomComplet;
}
