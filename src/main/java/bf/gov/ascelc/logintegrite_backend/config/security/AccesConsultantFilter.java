package bf.gov.ascelc.logintegrite_backend.config.security;

import bf.gov.ascelc.logintegrite_backend.common.security.ProfilUtilisateur;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Refus PAR DEFAUT pour un compte de consultation seule : il n'a le droit d'appeler que l'API de verification
 * (recherche d'une personne precise, fiche limitee au registre officiel, demandes d'export) et le signalement
 * d'evenements de son propre poste. Tout le reste de l'API (listes de personnes et de dossiers, faits en attente,
 * documents, audit...) lui est ferme cote serveur, quelle que soit l'interface utilisee.
 *
 * Un compte qui cumule un autre role (agent, validateur, administrateur) n'est PAS concerne.
 * Cree dans SecurityConfig (et non declare comme composant) pour ne pas etre enregistre deux fois.
 */
class AccesConsultantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (estConsultantSeul(authentication) && !autorise(request.getMethod(), request.getRequestURI())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/problem+json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"title\":\"Acces refuse\",\"status\":403,"
                    + "\"detail\":\"Ce compte de consultation n'a pas acces a cette ressource.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    static boolean estConsultantSeul(Authentication authentication) {
        return ProfilUtilisateur.estConsultantSeul(authentication);
    }

    /** Liste blanche : seuls ces appels sont permis a un compte de consultation seule. */
    static boolean autorise(String methode, String chemin) {
        if (chemin.equals("/api/v1/verification") || chemin.startsWith("/api/v1/verification/")) return true;
        return HttpMethod.POST.matches(methode) && chemin.equals("/api/v1/audit/evenements-poste");
    }
}
