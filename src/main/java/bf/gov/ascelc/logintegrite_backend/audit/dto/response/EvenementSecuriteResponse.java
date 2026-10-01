package bf.gov.ascelc.logintegrite_backend.audit.dto.response;

import java.time.Instant;
import java.util.UUID;

public record EvenementSecuriteResponse(
        UUID id,
        Instant date,
        String type,
        String utilisateur,
        String page,
        String detail,
        String adresseIp,
        String userAgent) { }
