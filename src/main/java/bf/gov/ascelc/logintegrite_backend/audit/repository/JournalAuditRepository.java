package bf.gov.ascelc.logintegrite_backend.audit.repository;

import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalAudit;
import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalAuditId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, JournalAuditId> {
    Page<JournalAudit> findAllByOrderByDateActionDesc(Pageable pageable);
    Page<JournalAudit> findByEntiteCibleOrderByDateActionDesc(String entiteCible, Pageable pageable);
}
