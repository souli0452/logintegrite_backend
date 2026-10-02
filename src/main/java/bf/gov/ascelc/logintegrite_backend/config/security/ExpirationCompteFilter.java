package bf.gov.ascelc.logintegrite_backend.config.security;

import bf.gov.ascelc.logintegrite_backend.securite.service.ControleExpirationCompte;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Refuse (403) toute requete d'un compte dont la date d'expiration est depassee, sans attendre que le jeton expire
 * ni que la desactivation dans Keycloak soit passee. Cree dans SecurityConfig (et non declare comme composant).
 */
class ExpirationCompteFilter extends OncePerRequestFilter {

    private final ControleExpirationCompte controle;

    ExpirationCompteFilter(ControleExpirationCompte controle) {
        this.controle = controle;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jeton && controle.estExpire(jeton.getToken().getSubject())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/problem+json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"title\":\"Compte expire\",\"status\":403,"
                    + "\"detail\":\"Ce compte a expire. Contactez l'administrateur pour le prolonger.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
