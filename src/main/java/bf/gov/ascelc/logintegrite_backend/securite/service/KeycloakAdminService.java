package bf.gov.ascelc.logintegrite_backend.securite.service;

import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service d'orchestration avec l'API Admin REST de Keycloak.
 * Utilise RestClient natif de Spring, sans dependance externe.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    @Value("${keycloak.admin.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    private RestClient tokenClient;
    private RestClient adminClient;

    // Cache simple de token (renouvelle 30s avant expiration)
    private final AtomicReference<String> tokenCache = new AtomicReference<>();
    private volatile Instant tokenExpiration = Instant.EPOCH;

    @PostConstruct
    public void init() {
        this.tokenClient = RestClient.builder()
                .baseUrl(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .build();
        this.adminClient = RestClient.builder()
                .baseUrl(serverUrl + "/admin/realms/" + realm)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("KeycloakAdminService initialise pour realm={} url={}", realm, serverUrl);
    }

    // ==================== TOKEN ====================
    @SuppressWarnings("unchecked")
    private String getAdminToken() {
        if (tokenCache.get() != null && Instant.now().isBefore(tokenExpiration)) {
            return tokenCache.get();
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        Map<String, Object> response = tokenClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Impossible d'obtenir un token admin Keycloak");
        }
        String token = (String) response.get("access_token");
        int expiresIn = ((Number) response.getOrDefault("expires_in", 60)).intValue();

        tokenCache.set(token);
        tokenExpiration = Instant.now().plusSeconds(Math.max(30, expiresIn - 30));
        return token;
    }

    // ==================== CREATION UTILISATEUR ====================
    @SuppressWarnings("unchecked")
    public String creerUtilisateur(String nom, String prenom, String email,
                                    String motDePasseTemporaire, CodeRole role) {

        // 1. Verifier si l'email existe deja
        List<Map<String, Object>> existants = adminClient.get()
                .uri("/users?email={email}&exact=true", email)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAdminToken())
                .retrieve()
                .body(List.class);

        if (existants != null && !existants.isEmpty()) {
            throw new IllegalStateException("Un utilisateur avec cet email existe deja dans Keycloak : " + email);
        }

        // 2. Creer l'utilisateur
        Map<String, Object> payload = Map.of(
                "username", email,
                "email", email,
                "firstName", prenom,
                "lastName", nom,
                "enabled", true,
                "emailVerified", false,
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", motDePasseTemporaire,
                        "temporary", true
                )),
                "requiredActions", List.of("UPDATE_PASSWORD")
        );

        String locationHeader;
        try {
            var response = adminClient.post()
                    .uri("/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAdminToken())
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            var location = response.getHeaders().getLocation();
            if (location == null) {
                throw new IllegalStateException("Keycloak n'a pas retourne l'URL de l'utilisateur cree");
            }
            locationHeader = location.toString();
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("Echec creation Keycloak (HTTP " + e.getStatusCode() + ") : " +
                    e.getResponseBodyAsString(), e);
        }

        // Extraire l'id Keycloak
        String keycloakId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);

        // 3. Attribuer le role
        attribuerRole(keycloakId, role);

        log.info("Utilisateur Keycloak cree : keycloakId={}, email={}, role={}", keycloakId, email, role);
        return keycloakId;
    }

    // ==================== ATTRIBUER / RETIRER ROLE ====================
    public void attribuerRole(String keycloakId, CodeRole role) {
        try {
            Map<String, Object> roleObj = getRealmRole(role);
            if (roleObj == null) {
                log.warn("Role {} introuvable dans Keycloak, attribution ignoree", role);
                return;
            }
            adminClient.post()
                    .uri("/users/{id}/role-mappings/realm", keycloakId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAdminToken())
                    .body(List.of(roleObj))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Impossible d'attribuer le role {} a {} : {}", role, keycloakId, e.getMessage());
        }
    }

    public void retirerRole(String keycloakId, CodeRole role) {
        try {
            Map<String, Object> roleObj = getRealmRole(role);
            if (roleObj == null) return;
            adminClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/users/{id}/role-mappings/realm", keycloakId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAdminToken())
                    .body(List.of(roleObj))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Impossible de retirer le role {} a {} : {}", role, keycloakId, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getRealmRole(CodeRole role) {
        try {
            return adminClient.get()
                    .uri("/roles/{roleName}", role.name())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAdminToken())
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.warn("Role {} non trouve dans Keycloak", role);
            return null;
        }
    }

    // ==================== ACTIVATION ====================
    public void modifierActivation(String keycloakId, boolean actif) {
        try {
            adminClient.put()
                    .uri("/users/{id}", keycloakId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAdminToken())
                    .body(Map.of("enabled", actif))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Utilisateur {} {} dans Keycloak", keycloakId, actif ? "active" : "desactive");
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("Echec modification activation (HTTP " + e.getStatusCode() + ") : " +
                    e.getResponseBodyAsString(), e);
        }
    }

    // ==================== SUPPRESSION ====================
    public void supprimerUtilisateur(String keycloakId) {
        try {
            adminClient.delete()
                    .uri("/users/{id}", keycloakId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAdminToken())
                    .retrieve()
                    .toBodilessEntity();
            log.info("Utilisateur {} supprime de Keycloak", keycloakId);
        } catch (Exception e) {
            log.warn("Impossible de supprimer {} de Keycloak : {}", keycloakId, e.getMessage());
        }
    }
}
