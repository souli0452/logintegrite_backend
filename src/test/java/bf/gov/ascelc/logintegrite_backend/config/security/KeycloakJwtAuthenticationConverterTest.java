package bf.gov.ascelc.logintegrite_backend.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtAuthenticationConverterTest {

    private final KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();

    private static Jwt jwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("t").header("alg", "none").subject("u1")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60))
                .claims(c -> c.putAll(claims)).build();
    }

    @Test
    void convertitLesRolesRealmEnAutoritesPrefixeesEnMajuscules() {
        var auth = converter.convert(jwt(Map.of("realm_access", Map.of("roles", List.of("ADMIN", "agent")))));

        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_AGENT");
    }

    @Test
    void neDonneAucuneAutoriteSansClaimRealmAccess() {
        assertThat(converter.convert(jwt(Map.of("scope", "openid"))).getAuthorities()).isEmpty();
    }

    @Test
    void neDonneAucuneAutoriteSiLaListeDeRolesEstAbsente() {
        assertThat(converter.convert(jwt(Map.of("realm_access", Map.of("autre", "x")))).getAuthorities()).isEmpty();
    }
}
