package bf.gov.ascelc.logintegrite_backend.audit.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalConsultation;
import bf.gov.ascelc.logintegrite_backend.audit.event.ConsultationEvent;
import bf.gov.ascelc.logintegrite_backend.audit.repository.JournalConsultationRepository;
import bf.gov.ascelc.logintegrite_backend.audit.service.ConsultationService;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUserProvider currentUserProvider;
    private final JournalConsultationRepository repository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // Correction ici : création d'une nouvelle transaction
    public void enregistrer(String entiteConsultee, UUID entiteConsulteeId) {
        // On lit l'ID de l'utilisateur dans la transaction principale et on
        // le transmet en primitif. Aucun proxy Hibernate ne circule dans
        // l'evenement - c'est la garantie qu'il ne pourra plus y avoir de
        // LazyInitException dans le listener.
        Utilisateur user = currentUserProvider.utilisateurCourant();
        UUID userId = user.getId();
        eventPublisher.publishEvent(new ConsultationEvent(entiteConsultee, entiteConsulteeId, userId));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void surConsultation(ConsultationEvent evenement) {
        Utilisateur utilisateur = utilisateurRepository.findById(evenement.utilisateurId()).orElse(null);
        if (utilisateur == null) return;

        JournalConsultation consultation = new JournalConsultation();
        consultation.setId(UUID.randomUUID());
        consultation.setUtilisateur(utilisateur);
        consultation.setEntiteConsultee(evenement.entiteConsultee());
        consultation.setEntiteConsulteeId(evenement.entiteConsulteeId());
        consultation.setDateConsultation(Instant.now());
        repository.save(consultation);
    }
}
