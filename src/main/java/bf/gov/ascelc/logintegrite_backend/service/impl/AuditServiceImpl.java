package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.entity.JournalAudit;
import bf.gov.ascelc.logintegrite_backend.repository.JournalAuditRepository;
import bf.gov.ascelc.logintegrite_backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final JournalAuditRepository auditRepo;

    @Override
    public void log(Jwt jwt, String action, String typeEntite, String entiteId, String details, String ipAdresse) {
        // Extraction des informations utilisateur depuis le jeton Keycloak (JWT)
        String userId = jwt != null ? jwt.getSubject() : "SYSTEM";
        String username = jwt != null ? jwt.getClaimAsString("preferred_username") : "SYSTEM";

        // Construction de l'objet d'audit avec les correspondances exactes de tes propriétés
        JournalAudit log = new JournalAudit();
        log.setUtilisateurId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setRessourceType(typeEntite);
        log.setRessourceId(entiteId);
        log.setDescription(details); // 'details' mappe sur 'description'
        log.setAdresseIp(ipAdresse);   // 'ipAdresse' mappe sur 'adresseIp'
        log.setHorodatage(LocalDateTime.now());

        auditRepo.save(log);
    }
}