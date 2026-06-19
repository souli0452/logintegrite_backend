package bf.gov.ascelc.logintegrite_backend.config.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication
    .JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.Optional;


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
