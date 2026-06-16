package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.JournalAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface JournalAuditRepository
        extends JpaRepository<JournalAudit, UUID> { // Changé de Long à UUID

    Page<JournalAudit> findByUtilisateurId(
            String utilisateurId, Pageable pageable);

    Page<JournalAudit> findByAction(
            String action, Pageable pageable);
}