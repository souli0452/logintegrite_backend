package bf.gov.ascelc.logintegrite_backend.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.*;
import java.util.stream.Collectors;

public class KeycloakJwtConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(
            jwt,
            extractRoles(jwt),
            jwt.getClaimAsString("preferred_username")
        );
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess =
            jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) return Collections.emptyList();
        List<String> roles =
            (List<String>) realmAccess.getOrDefault("roles", List.of());
        return roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
            .collect(Collectors.toList());
    }
}
