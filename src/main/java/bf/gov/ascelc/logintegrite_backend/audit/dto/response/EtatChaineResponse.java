package bf.gov.ascelc.logintegrite_backend.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Instantané de l'état de la chaîne d'audit — alimente le bandeau d'intégrité
 * cryptographique en haut de l'écran forensique.
 *
 * <p>Aucun calcul de hash n'est effectué à ce niveau : les valeurs sont lues
 * telles quelles dans la table {@code audit.journal_audit} via la fonction SQL
 * {@code audit.etat_chaine()}. La vérification cryptographique effective est
 * exposée séparément par {@link VerificationChaineResponse}.
 */
@Getter
@Builder
@AllArgsConstructor
public class EtatChaineResponse {

    /** Nombre total d'entrées scellées dans la chaîne. */
    private long totalEntrees;

    /** Hash de la dernière ligne insérée — signature courante de la chaîne. */
    private String dernierHash;

    /** Horodatage de la dernière action tracée. */
    private Instant dateDerniereAction;

    /** Horodatage de la première action tracée — début de la chaîne. */
    private Instant datePremiereAction;

    /** Nombre distinct d'utilisateurs ayant produit au moins une action. */
    private long totalUtilisateurs;

    /** Nombre distinct de types d'entités auditées (Personne, Dossier, etc.). */
    private long totalEntites;
}
