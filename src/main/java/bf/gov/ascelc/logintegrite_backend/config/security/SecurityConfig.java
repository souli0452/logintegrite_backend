package bf.gov.ascelc.logintegrite_backend.config.security;

import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.common.security.SynchroniseurRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** Roles applicatifs : un jeton valide sans aucun de ces roles n'ouvre aucun endpoint metier. */
    private static final String[] ROLES_APPLICATIFS = {"ADMIN", "AGENT", "VALIDATEUR", "CONSULTANT"};

    private final KeycloakJwtAuthenticationConverter jwtAuthenticationConverter;

    @Value("${logintegrite.security.cors.allowed-origins:http://localhost:4200}")
    private List<String> originesAutorisees;

    @Value("${springdoc.swagger-ui.enabled:false}")
    private boolean swaggerActif;

    /**
     * Decodeur JWT : signature (cles JWK), emetteur ET audience. L'URL des cles est distincte de
     * l'emetteur pour que le backend puisse les lire via le reseau interne (Docker).
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
            @Value("${logintegrite.security.audience}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                new AudienceValidator(audience)));
        return decoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CurrentUserProvider currentUserProvider,
                                           SynchroniseurRoles synchroniseurRoles,
                                           bf.gov.ascelc.logintegrite_backend.securite.service.ControleExpirationCompte controleExpiration) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // API stateless, jeton Bearer, pas de cookie de session
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                if (swaggerActif) {
                    auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll();
                }
                auth.anyRequest().hasAnyRole(ROLES_APPLICATIFS);
            })
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            )
            // Provisionne le compte local et reporte ses roles des la premiere requete (une fois par utilisateur).
            .addFilterAfter(new SynchronisationUtilisateurFilter(currentUserProvider, synchroniseurRoles),
                    BearerTokenAuthenticationFilter.class)
            // Compte de consultation seule : liste blanche d'appels (voir AccesConsultantFilter).
            .addFilterAfter(new AccesConsultantFilter(), SynchronisationUtilisateurFilter.class)
            // Compte expire : refuse des la requete suivante, sans attendre la fin du jeton.
            .addFilterAfter(new ExpirationCompteFilter(controleExpiration), AccesConsultantFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(originesAutorisees);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
