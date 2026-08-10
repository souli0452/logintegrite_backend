package bf.gov.ascelc.logintegrite_backend.audit.repository;

import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalConsultation;
import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalConsultationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalConsultationRepository extends JpaRepository<JournalConsultation, JournalConsultationId> {
    Page<JournalConsultation> findAllByOrderByDateConsultationDesc(Pageable pageable);
    Page<JournalConsultation> findByEntiteConsulteeOrderByDateConsultationDesc(String entiteConsultee, Pageable pageable);
}
