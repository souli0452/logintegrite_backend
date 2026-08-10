package bf.gov.ascelc.logintegrite_backend.audit.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.JournalAuditResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.JournalConsultationResponse;
import bf.gov.ascelc.logintegrite_backend.audit.mapper.JournalAuditMapper;
import bf.gov.ascelc.logintegrite_backend.audit.mapper.JournalConsultationMapper;
import bf.gov.ascelc.logintegrite_backend.audit.repository.JournalAuditRepository;
import bf.gov.ascelc.logintegrite_backend.audit.repository.JournalConsultationRepository;
import bf.gov.ascelc.logintegrite_backend.audit.service.AuditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditQueryServiceImpl implements AuditQueryService {

    private final JournalAuditRepository journalAuditRepository;
    private final JournalConsultationRepository journalConsultationRepository;
    private final JournalAuditMapper journalAuditMapper;
    private final JournalConsultationMapper journalConsultationMapper;

    @Override
    public Page<JournalAuditResponse> listerAudit(String entiteCible, Pageable pageable) {
        Page<bf.gov.ascelc.logintegrite_backend.audit.entity.JournalAudit> page =
                StringUtils.hasText(entiteCible)
                        ? journalAuditRepository.findByEntiteCibleOrderByDateActionDesc(entiteCible, pageable)
                        : journalAuditRepository.findAllByOrderByDateActionDesc(pageable);
        return page.map(journalAuditMapper::toResponse);
    }

    @Override
    public Page<JournalConsultationResponse> listerConsultations(String entiteConsultee, Pageable pageable) {
        Page<bf.gov.ascelc.logintegrite_backend.audit.entity.JournalConsultation> page =
                StringUtils.hasText(entiteConsultee)
                        ? journalConsultationRepository.findByEntiteConsulteeOrderByDateConsultationDesc(entiteConsultee, pageable)
                        : journalConsultationRepository.findAllByOrderByDateConsultationDesc(pageable);
        return page.map(journalConsultationMapper::toResponse);
    }
}
