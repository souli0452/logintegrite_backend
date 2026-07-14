package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

// Point d'entrée UNIQUE pour les effets de bord (audit + notification) des 3
// évènements du cycle de vie d'une fiche. Remplace les triplets
// auditService.log(...) + notificationService.notifier...() auparavant
// copiés-collés dans FicheMiseEnCauseController, PersonnePhysiqueController
// et PersonneMoraleController. Pas d'interface : orchestration interne pure,
// aucune implémentation alternative n'est attendue.
@Component
@RequiredArgsConstructor
public class FicheEvenementNotifier {

    private final AuditService auditService;
    private final NotificationService notificationService;

    public void soumission(Jwt jwt, HttpServletRequest request, FicheMiseEnCauseResponse response, String nomCible) {
        String username = jwt != null ? jwt.getClaimAsString("preferred_username") : "SYSTEM";
        auditService.log(jwt, "SOUMISSION_FICHE", response.getTypeFiche(), response.getId().toString(), null, request.getRemoteAddr());
        notificationService.notifierRole("VALIDATEUR", "SOUMISSION_FICHE",
                "La fiche " + nomCible + " a été soumise par " + username + " et attend votre validation.",
                response.getId().toString(), response.getTypeFiche());
    }

    public void validation(Jwt jwt, HttpServletRequest request, FicheMiseEnCauseResponse response, String nomCible) {
        auditService.log(jwt, "VALIDATION_FICHE", response.getTypeFiche(), response.getId().toString(), null, request.getRemoteAddr());
        notificationService.notifierUtilisateur(response.getCreatedById(), "VALIDATION_FICHE",
                "Votre fiche " + nomCible + " a été validée.",
                response.getId().toString(), response.getTypeFiche());
    }

    public void rejet(Jwt jwt, HttpServletRequest request, FicheMiseEnCauseResponse response, String nomCible, String motif) {
        auditService.log(jwt, "REJET_FICHE", response.getTypeFiche(), response.getId().toString(), motif, request.getRemoteAddr());
        notificationService.notifierUtilisateur(response.getCreatedById(), "REJET_FICHE",
                "Votre fiche " + nomCible + " a été rejetée. Motif : " + motif,
                response.getId().toString(), response.getTypeFiche());
    }
}
