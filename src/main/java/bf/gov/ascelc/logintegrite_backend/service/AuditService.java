package bf.gov.ascelc.logintegrite_backend.service;

import org.springframework.security.oauth2.jwt.Jwt;

public interface AuditService {
    void log(Jwt jwt, String action, String typeEntite, String entiteId, String details, String ipAdresse);
}