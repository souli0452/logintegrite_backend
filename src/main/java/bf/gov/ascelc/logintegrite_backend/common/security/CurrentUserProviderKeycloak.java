package bf.gov.ascelc.logintegrite_backend.common.security;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class CurrentUserProviderKeycloak implements CurrentUserProvider {

    private final UtilisateurRepository utilisateurRepository;
    private final TransactionTemplate transactionEcriture;
    private final SynchroniseurRoles synchroniseurRoles;

    public CurrentUserProviderKeycloak(UtilisateurRepository utilisateurRepository,
                                       PlatformTransactionManager transactionManager,
                                       SynchroniseurRoles synchroniseurRoles) {
        this.utilisateurRepository = utilisateurRepository;
        this.synchroniseurRoles = synchroniseurRoles;
        // REQUIRES_NEW : transaction ecrivable independante, meme si l'appelant est en readOnly
        // (un GET qui provisionne l'utilisateur ne doit pas echouer sur "cannot INSERT in
        // read-only transaction"). TransactionTemplate et non @Transactional : l'auto-appel
        // d'une methode annotee contourne le proxy Spring et l'annotation serait sans effet.
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionEcriture = template;
    }

    @Override
    public Utilisateur utilisateurCourant() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String keycloakId = jwt.getSubject();

        Utilisateur utilisateur = utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> provisionner(jwt, keycloakId));
        synchroniseurRoles.synchroniser(utilisateur, jwt);
        return utilisateur;
    }

    private Utilisateur provisionner(Jwt jwt, String keycloakId) {
        try {
            return transactionEcriture.execute(status -> creerDepuisJwt(jwt, keycloakId));
        } catch (DataIntegrityViolationException e) {
            // Requete concurrente : un autre thread a provisionne le meme utilisateur entre-temps.
            return utilisateurRepository.findByKeycloakId(keycloakId).orElseThrow(() -> e);
        }
    }

    private Utilisateur creerDepuisJwt(Jwt jwt, String keycloakId) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setKeycloakId(keycloakId);
        utilisateur.setNom(valeurOuDefaut(jwt.getClaimAsString("family_name"), "Inconnu"));
        utilisateur.setPrenom(valeurOuDefaut(jwt.getClaimAsString("given_name"), "Inconnu"));
        utilisateur.setEmail(valeurOuDefaut(jwt.getClaimAsString("email"), keycloakId + "@inconnu.local"));
        utilisateur.setActif(true);
        return utilisateurRepository.save(utilisateur);
    }

    private String valeurOuDefaut(String valeur, String defaut) {
        return valeur != null ? valeur : defaut;
    }
}
