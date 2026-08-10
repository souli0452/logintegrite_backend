package bf.gov.ascelc.logintegrite_backend.common.security;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CurrentUserProviderKeycloak implements CurrentUserProvider {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public Utilisateur utilisateurCourant() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String keycloakId = jwt.getSubject();

        return utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> creerDepuisJwt(jwt, keycloakId));
    }

    // REQUIRES_NEW : force une nouvelle transaction ecrivable, meme si la
    // methode appelante est en readOnly. Sans ca, une lecture (GET) qui
    // provoque le provisionnement JIT echouerait sur "cannot INSERT in
    // read-only transaction".
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Utilisateur creerDepuisJwt(Jwt jwt, String keycloakId) {
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
