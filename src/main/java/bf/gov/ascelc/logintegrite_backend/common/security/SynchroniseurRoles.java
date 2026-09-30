package bf.gov.ascelc.logintegrite_backend.common.security;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import bf.gov.ascelc.logintegrite_backend.securite.entity.UtilisateurRole;
import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import bf.gov.ascelc.logintegrite_backend.securite.repository.RoleHabilitationRepository;
import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reporte dans la table locale les roles de l'utilisateur presents dans son jeton Keycloak.
 *
 * Les autorisations reposent sur le jeton ; la table locale sert a l'affichage (ecran Administration) et a
 * l'audit. Un compte cree directement dans Keycloak (par exemple le premier administrateur, cree par script)
 * n'y a aucun lien : sans cette synchronisation, l'ecran afficherait "Aucun role".
 * On ajoute les roles manquants, on n'en retire jamais (la gestion des roles reste a l'administrateur).
 */
@Component
public class SynchroniseurRoles {

    private static final Logger log = LoggerFactory.getLogger(SynchroniseurRoles.class);

    private final UtilisateurRoleRepository liens;
    private final RoleHabilitationRepository roles;
    private final TransactionTemplate transactionEcriture;
    /** Utilisateurs deja synchronises depuis le demarrage : evite une requete a chaque appel. */
    private final Set<String> synchronises = ConcurrentHashMap.newKeySet();
    /** Un verrou par utilisateur : les requetes simultanees de sa premiere connexion attendent la premiere. */
    private final Map<String, Object> verrous = new ConcurrentHashMap<>();

    public SynchroniseurRoles(UtilisateurRoleRepository liens, RoleHabilitationRepository roles,
                              PlatformTransactionManager transactionManager) {
        this.liens = liens;
        this.roles = roles;
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionEcriture = template;
    }

    public boolean dejaSynchronise(String keycloakId) {
        return synchronises.contains(keycloakId);
    }

    public void synchroniser(Utilisateur utilisateur, Jwt jwt) {
        String keycloakId = utilisateur.getKeycloakId();
        if (utilisateur.getId() == null || keycloakId == null || synchronises.contains(keycloakId)) {
            return;
        }
        List<CodeRole> codes = codesDuJeton(jwt);
        synchronized (verrous.computeIfAbsent(keycloakId, cle -> new Object())) {
            if (synchronises.contains(keycloakId)) {
                return;   // une requete simultanee vient de le faire
            }
            try {
                transactionEcriture.executeWithoutResult(status -> ajouterLiensManquants(utilisateur, codes));
                synchronises.add(keycloakId);
                verrous.remove(keycloakId);
            } catch (RuntimeException e) {
                // Ne jamais faire echouer la requete metier pour un simple affichage : on reessaiera au prochain appel.
                log.warn("Synchronisation des roles impossible pour l'utilisateur {} : {}", utilisateur.getId(), e.getMessage());
            }
        }
    }

    private void ajouterLiensManquants(Utilisateur utilisateur, List<CodeRole> codes) {
        for (CodeRole code : codes) {
            roles.findByCode(code).ifPresent(role -> {
                if (!liens.existsByUtilisateur_IdAndRoleHabilitation_Id(utilisateur.getId(), role.getId())) {
                    UtilisateurRole lien = new UtilisateurRole();
                    lien.setUtilisateur(utilisateur);
                    lien.setRoleHabilitation(role);
                    liens.save(lien);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    public static List<CodeRole> codesDuJeton(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> valeurs)) {
            return List.of();
        }
        return ((List<Object>) valeurs).stream()
                .map(v -> codeOuNull(v.toString()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    private static CodeRole codeOuNull(String nom) {
        try {
            return CodeRole.valueOf(nom.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;   // role Keycloak sans equivalent metier (offline_access, uma_authorization...)
        }
    }
}
