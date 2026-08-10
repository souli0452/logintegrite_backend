package bf.gov.ascelc.logintegrite_backend.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Résultat de la vérification d'un maillon isolé, retrouvé par son hash
 * (complet ou préfixe hexadécimal).
 *
 * <p>Utilisé par le drawer « Vérifier un hash » de l'écran forensique — permet à
 * un auditeur externe de copier-coller un hash observé sur un rapport papier
 * pour confirmer que la ligne existe et n'a pas été altérée.
 *
 * <p>Réponse HTTP 404 attendue si aucun maillon ne correspond au hash fourni.
 */
@Getter
@Builder
@AllArgsConstructor
public class VerificationMaillonResponse {

    /** Identifiant du maillon localisé. */
    private UUID id;

    /** Horodatage exact du maillon. */
    private Instant dateAction;

    /** Action tracée sur ce maillon. */
    private String action;

    /** Entité cible auditée par ce maillon. */
    private String entiteCible;

    /** Hash tel que stocké en base. */
    private String hashStocke;

    /** Hash tel que recalculé par le serveur — doit être identique à {@link #hashStocke}. */
    private String hashRecalcule;

    /** Hash du maillon précédent, tel que stocké dans ce maillon. */
    private String hashPrecedent;

    /**
     * {@code true} si {@code hashStocke == hashRecalcule} ET si le chaînage
     * vers le maillon précédent est correct.
     */
    private boolean integre;

    /** Rang chronologique du maillon dans la chaîne (1-based). */
    private long positionDansChaine;
}
