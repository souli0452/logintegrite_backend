package bf.gov.ascelc.logintegrite_backend.config.security;

import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.common.security.SynchroniseurRoles;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A la premiere requete authentifiee d'un utilisateur (par demarrage), cree son compte local et reporte ses roles :
 * l'ecran Administration est ainsi juste meme pour un compte cree directement dans Keycloak. Les requetes suivantes
 * ne coutent rien (l'utilisateur est deja connu). Un echec ici ne bloque jamais la requete.
 * Cree dans SecurityConfig (et non declare comme composant) pour ne pas etre enregistre deux fois par Spring Boot.
 */
class SynchronisationUtilisateurFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SynchronisationUtilisateurFilter.class);

    private final CurrentUserProvider currentUserProvider;
    private final SynchroniseurRoles synchroniseurRoles;

    SynchronisationUtilisateurFilter(CurrentUserProvider currentUserProvider, SynchroniseurRoles synchroniseurRoles) {
        this.currentUserProvider = currentUserProvider;
        this.synchroniseurRoles = synchroniseurRoles;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jeton
                && !SynchroniseurRoles.codesDuJeton(jeton.getToken()).isEmpty()
                && !synchroniseurRoles.dejaSynchronise(jeton.getToken().getSubject())) {
            try {
                currentUserProvider.utilisateurCourant();
            } catch (RuntimeException e) {
                log.warn("Provisionnement de l'utilisateur impossible : {}", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
