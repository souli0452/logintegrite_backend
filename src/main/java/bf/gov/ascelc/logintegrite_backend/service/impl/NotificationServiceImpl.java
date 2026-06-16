package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.entity.Notification;
import bf.gov.ascelc.logintegrite_backend.repository.NotificationRepository;
import bf.gov.ascelc.logintegrite_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notifRepo;

    @Override
    public void envoyer(String destinataireId, String type, String contenu) {
        notifRepo.save(Notification.builder()
                .destinataireId(destinataireId)
                .type(type)
                .contenu(contenu)
                .lue(false)
                .build());
    }

    @Override
    public void notifierValidateur(String validateurId, String nomFiche) {
        envoyer(validateurId, "VALIDATION_REQUISE",
                "Une fiche nécessite votre validation : " + nomFiche);
    }

    @Override
    public void notifierAgent(String agentId, String nomFiche, boolean validee, String motif) {
        String message = validee
                ? "Votre fiche a été validée : " + nomFiche
                : "Votre fiche a été rejetée : " + nomFiche + " — Motif : " + motif;
        envoyer(agentId, validee ? "FICHE_VALIDEE" : "FICHE_REJETEE", message);
    }

    @Override
    public long countNonLues(String utilisateurId) {
        return notifRepo.countByDestinataireIdAndLueFalse(utilisateurId);
    }

    @Override
    @Transactional
    public void marquerToutesLues(String utilisateurId) {
        notifRepo.marquerToutesLues(utilisateurId);
    }
}