package bf.gov.ascelc.logintegrite_backend.securite.service;

import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Un compte expire est refuse des la requete suivante, meme si son jeton est encore valide.
 * La date est lue en base, avec une courte memoire (60 s) pour ne pas interroger la base a chaque appel ;
 * toute modification de l'expiration ou de l'activation invalide immediatement cette memoire.
 */
@Component
@RequiredArgsConstructor
public class ControleExpirationCompte {

    static final Duration DUREE_MEMOIRE = Duration.ofSeconds(60);

    private record Entree(Optional<LocalDate> expiration, Instant valideJusqua) { }

    private final UtilisateurRepository utilisateurRepository;
    private final Map<String, Entree> memoire = new ConcurrentHashMap<>();

    /** Vrai si le compte (identifiant Keycloak) a une date d'expiration strictement anterieure a aujourd'hui. */
    public boolean estExpire(String keycloakId) {
        if (keycloakId == null) return false;
        return estExpire(keycloakId, LocalDate.now());
    }

    boolean estExpire(String keycloakId, LocalDate aujourdhui) {
        Entree e = memoire.get(keycloakId);
        if (e == null || Instant.now().isAfter(e.valideJusqua())) {
            Optional<LocalDate> date = utilisateurRepository.findByKeycloakId(keycloakId)
                    .map(u -> u.getDateExpiration());
            e = new Entree(date, Instant.now().plus(DUREE_MEMOIRE));
            memoire.put(keycloakId, e);
        }
        return e.expiration().map(d -> d.isBefore(aujourdhui)).orElse(false);
    }

    public void invalider(String keycloakId) {
        if (keycloakId != null) memoire.remove(keycloakId);
    }
}
