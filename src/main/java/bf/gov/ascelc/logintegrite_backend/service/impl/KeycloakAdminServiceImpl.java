package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.config.security.KeycloakAdminProperties;
import bf.gov.ascelc.logintegrite_backend.service.KeycloakAdminService;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

// Intégration Keycloak Admin REST API : seule source de vérité pour résoudre
// "tous les utilisateurs ayant le rôle VALIDATEUR" (aucune table Utilisateur
// en base). Prérequis Keycloak : client confidentiel avec Service Accounts
// activé + rôle client "view-users" de "realm-management" assigné.
@Service
public class KeycloakAdminServiceImpl implements KeycloakAdminService {

    private final KeycloakAdminProperties properties;
    private final RestTemplate restTemplate;

    private final AtomicReference<String> tokenCache = new AtomicReference<>();
    private volatile long tokenExpirationEpochMs = 0L;

    public KeycloakAdminServiceImpl(KeycloakAdminProperties properties, RestTemplateBuilder builder) {
        this.properties = properties;
        this.restTemplate = builder.build();
    }

    @Override
    public List<String> listerIdsUtilisateursParRole(String role) {
        String token = obtenirTokenAdmin();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = properties.getServerUrl() + "/admin/realms/" + properties.getRealm()
                + "/roles/" + role + "/users";

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> utilisateurs = response.getBody();
        if (utilisateurs == null) {
            return Collections.emptyList();
        }

        return utilisateurs.stream()
                .filter(u -> Boolean.TRUE.equals(u.get("enabled")))
                .map(u -> (String) u.get("id"))
                .filter(Objects::nonNull)
                .toList();
    }

    // Cache mémoire du token admin (évite un aller-retour Keycloak à chaque
    // soumission de fiche). Rafraîchi 10s avant expiration réelle.
    private synchronized String obtenirTokenAdmin() {
        long maintenant = System.currentTimeMillis();
        String tokenActuel = tokenCache.get();
        if (tokenActuel != null && maintenant < tokenExpirationEpochMs) {
            return tokenActuel;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        String url = properties.getServerUrl() + "/realms/" + properties.getRealm()
                + "/protocol/openid-connect/token";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> corps = response.getBody();
        String accessToken = corps != null ? (String) corps.get("access_token") : null;
        Number expiresIn = corps != null ? (Number) corps.get("expires_in") : null;

        if (accessToken == null) {
            throw new IllegalStateException("Impossible d'obtenir un token admin Keycloak.");
        }

        tokenCache.set(accessToken);
        long dureeMs = (expiresIn != null ? expiresIn.longValue() : 60) * 1000L;
        tokenExpirationEpochMs = maintenant + dureeMs - 10_000L;

        return accessToken;
    }
}