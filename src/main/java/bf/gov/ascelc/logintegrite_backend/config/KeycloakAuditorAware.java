package bf.gov.ascelc.logintegrite_backend.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication
    .JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Fournit automatiquement l'ID Keycloak de l'agent connecté
 * aux annotations @CreatedBy et @LastModifiedBy de JPA Auditing.
 * Plus besoin de setter manuellement createurId dans les services.
 */
@Component("keycloakAuditorAware")
public class KeycloakAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            // Retourne le Subject du JWT = UUID Keycloak de l'agent
            return Optional.ofNullable(
                jwtAuth.getToken().getSubject());
        }
        return Optional.empty();
    }
}
