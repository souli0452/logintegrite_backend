package bf.gov.ascelc.logintegrite_backend.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Résultat d'une vérification complète (ou partielle) de la chaîne d'audit.
 *
 * <p>Une chaîne est considérée intègre lorsque {@link #maillonsRompus} est vide.
 * Chaque entrée de cette liste identifie précisément un maillon dont soit le
 * hash stocké ne correspond plus au recalcul, soit le chaînage avec le maillon
 * précédent est rompu, soit les deux.
 */
@Getter
@Builder
@AllArgsConstructor
public class VerificationChaineResponse {

    /** {@code true} si aucun maillon rompu détecté sur le périmètre vérifié. */
    private boolean chaineIntegre;

    /** Nombre total de maillons parcourus par la vérification. */
    private long maillonsVerifies;

    /** Nombre de maillons rompus détectés (0 si intègre). */
    private long nombreRuptures;

    /** Horodatage de la vérification côté serveur. */
    private Instant dateVerification;

    /** Durée d'exécution de la vérification, en millisecondes. */
    private long dureeMillisecondes;

    /** Liste des maillons rompus — vide si la chaîne est intègre. */
    private List<MaillonRompu> maillonsRompus;

    /**
     * Description d'un maillon rompu détecté par la fonction SQL
     * {@code audit.verifier_integrite_chaine()}.
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class MaillonRompu {

        /** Identifiant du maillon rompu. */
        private UUID id;

        /** Horodatage du maillon. */
        private Instant dateAction;

        /** Action tracée sur ce maillon. */
        private String action;

        /** Entité cible du maillon. */
        private String entiteCible;

        /** Hash tel que recalculé par le serveur. */
        private String hashAttendu;

        /** Hash tel que stocké en base — devrait être identique à {@link #hashAttendu}. */
        private String hashStocke;

        /** Hash du maillon précédent tel que stocké dans cette ligne. */
        private String hashPrecedent;

        /** Hash du maillon précédent tel que recalculé — devrait être identique à {@link #hashPrecedent}. */
        private String hashPrecedentAttendu;

        /**
         * Type de rupture : {@code HASH_LIGNE} (le hash de la ligne ne matche pas),
         * {@code CHAINAGE} (le pointeur vers le maillon précédent ne matche pas),
         * ou {@code LES_DEUX}.
         */
        private String typeRupture;
    }
}
