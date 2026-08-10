// audit/service/impl/AuditServiceImpl.java (complet, corrigé)
package bf.gov.ascelc.logintegrite_backend.audit.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalAudit;
import bf.gov.ascelc.logintegrite_backend.audit.repository.JournalAuditRepository;
import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final JournalAuditRepository repository;
    private final CurrentUserProvider currentUserProvider;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional
    public void enregistrer(String action, String entiteCible, UUID entiteCibleId,
                             Map<String, Object> valeurAvant, Map<String, Object> valeurApres) {
        JournalAudit journal = new JournalAudit();
        journal.setId(UUID.randomUUID());
        journal.setUtilisateur(currentUserProvider.utilisateurCourant());
        journal.setAction(action);
        journal.setEntiteCible(entiteCible);
        journal.setEntiteCibleId(entiteCibleId);
        journal.setValeurAvant(versJson(valeurAvant));
        journal.setValeurApres(versJson(valeurApres));
        journal.setDateAction(Instant.now());
        repository.save(journal);
    }

    private String versJson(Map<String, Object> valeurs) {
        if (valeurs == null) return null;
        try {
            return jsonMapper.writeValueAsString(valeurs);
        } catch (RuntimeException e) {
            return "{\"erreur\":\"serialisation impossible\"}";
        }
    }
}
