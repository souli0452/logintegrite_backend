// JournalAuditRepository.java
package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.JournalAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalAuditRepository
    extends JpaRepository<JournalAudit, Long> {

    Page<JournalAudit> findByUtilisateurId(
        String utilisateurId, Pageable pageable);

    Page<JournalAudit> findByAction(
        String action, Pageable pageable);
}
