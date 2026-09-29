package bf.gov.ascelc.logintegrite_backend.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator("logintegrite-api");

    private static Jwt jwt(List<String> audience) {
        Jwt.Builder b = Jwt.withTokenValue("t").header("alg", "none").subject("u")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60));
        if (audience != null) b.audience(audience);
        return b.build();
    }

    @Test
    void accepteUnJetonDestineAcetteApi() {
        assertThat(validator.validate(jwt(List.of("account", "logintegrite-api"))).hasErrors()).isFalse();
    }

    @Test
    void refuseUnJetonPourUneAutreApplication() {
        assertThat(validator.validate(jwt(List.of("account"))).hasErrors()).isTrue();
    }

    @Test
    void refuseUnJetonSansAudience() {
        assertThat(validator.validate(jwt(null)).hasErrors()).isTrue();
    }
}
