package bf.gov.ascelc.logintegrite_backend.securite.service;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Desactive les comptes dont la date d'expiration est depassee : dans l'application ET dans Keycloak (plus de connexion
 * possible). Le filtre ExpirationCompteFilter refuse deja ces comptes immediatement ; cette tache garde la base et Keycloak
 * en accord, et trace chaque desactivation dans le journal de securite.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpirationComptesJob {

    private final UtilisateurRepository utilisateurRepository;
    private final KeycloakAdminService keycloakAdmin;
    private final ControleExpirationCompte controle;
    private final JdbcTemplate jdbc;

    /** Une fois par heure (la premiere fois 2 minutes apres le demarrage) : un compte ne reste jamais actif plus d'une heure apres l'echeance. */
    @Scheduled(initialDelay = 120_000, fixedDelay = 3_600_000)
    public void desactiverLesComptesExpires() {
        int n = desactiver(LocalDate.now());
        if (n > 0) log.info("{} compte(s) expire(s) desactive(s)", n);
    }

    @Transactional
    public int desactiver(LocalDate aujourdhui) {
        List<Utilisateur> expires = utilisateurRepository.findByActifTrueAndDateExpirationBefore(aujourdhui);
        int desactives = 0;
        for (Utilisateur u : expires) {
            try {
                keycloakAdmin.modifierActivation(u.getKeycloakId(), false);
                u.setActif(false);
                utilisateurRepository.save(u);
                controle.invalider(u.getKeycloakId());
                jdbc.update("insert into audit.journal_securite (id, utilisateur_id, type_evenement, page, detail) values (?, ?, ?, ?, ?)",
                        UUID.randomUUID(), u.getId(), "COMPTE_EXPIRE", "/expiration",
                        "Compte desactive : expire le " + u.getDateExpiration());
                desactives++;
            } catch (RuntimeException e) {
                // Un echec (Keycloak injoignable...) ne bloque pas les autres comptes ; reessaye a l'heure suivante.
                log.warn("Desactivation du compte {} impossible : {}", u.getId(), e.getMessage());
            }
        }
        return desactives;
    }
}
