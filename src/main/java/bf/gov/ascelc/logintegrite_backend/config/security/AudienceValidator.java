package bf.gov.ascelc.logintegrite_backend.config.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Refuse tout jeton qui n'est pas destine a cette API (claim "aud"), meme s'il est signe par
 * notre Keycloak : un jeton emis pour une autre application du meme realm ne doit pas ouvrir l'API.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error ERREUR = new OAuth2Error(
            "invalid_token", "Le jeton n'est pas destine a cette API (audience invalide)", null);

    private final String audienceAttendue;

    public AudienceValidator(String audienceAttendue) {
        this.audienceAttendue = audienceAttendue;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        return jwt.getAudience() != null && jwt.getAudience().contains(audienceAttendue)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(ERREUR);
    }
}
