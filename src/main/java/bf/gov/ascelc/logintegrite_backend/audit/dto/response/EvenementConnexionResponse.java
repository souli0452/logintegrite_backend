package bf.gov.ascelc.logintegrite_backend.audit.dto.response;

import java.time.Instant;

/** Evenement d'authentification releve par Keycloak (connexion, deconnexion, echec). */
public record EvenementConnexionResponse(
        Instant date,
        String type,
        String utilisateur,
        String adresseIp,
        String motif,
        String session) { }
