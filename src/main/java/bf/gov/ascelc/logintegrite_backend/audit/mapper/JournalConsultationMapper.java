package bf.gov.ascelc.logintegrite_backend.audit.mapper;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.JournalConsultationResponse;
import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalConsultation;
import org.springframework.stereotype.Component;

@Component
public class JournalConsultationMapper {

    public JournalConsultationResponse toResponse(JournalConsultation entity) {
        return JournalConsultationResponse.builder()
                .id(entity.getId())
                .entiteConsultee(entity.getEntiteConsultee())
                .entiteConsulteeId(entity.getEntiteConsulteeId())
                .dateConsultation(entity.getDateConsultation())
                .adresseIp(entity.getAdresseIp())
                .utilisateurId(entity.getUtilisateur() != null ? entity.getUtilisateur().getId() : null)
                .utilisateurNomComplet(entity.getUtilisateur() != null
                        ? entity.getUtilisateur().getPrenom() + " " + entity.getUtilisateur().getNom()
                        : null)
                .build();
    }
}
