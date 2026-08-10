package bf.gov.ascelc.logintegrite_backend.audit.service;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.EtatChaineResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.VerificationChaineResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.VerificationMaillonResponse;

import java.util.Optional;

/**
 * Service dédié à la vérification cryptographique de la chaîne d'audit.
 *
 * <p>Ne modifie <strong>jamais</strong> les tables du schéma {@code audit} —
 * les seules opérations exposées sont des lectures et des recalculs de hash
 * réalisés côté PostgreSQL via des fonctions {@code IMMUTABLE} / {@code STABLE}.
 *
 * <p>Alimente trois zones de l'écran forensique :
 * <ul>
 *   <li>le bandeau d'état de la chaîne (haut d'écran) via {@link #etatChaine()} ;</li>
 *   <li>le bouton « Vérifier l'intégrité maintenant » via {@link #verifierChaineComplete(Long)} ;</li>
 *   <li>le drawer « Vérifier un hash » via {@link #verifierMaillonParHash(String)}.</li>
 * </ul>
 */
public interface AuditIntegriteService {

    /**
     * Instantané synthétique de la chaîne (total, dernier hash, extrêmes,
     * cardinalités). Aucune vérification cryptographique effectuée.
     */
    EtatChaineResponse etatChaine();

    /**
     * Rejoue la chaîne (ou ses N premiers maillons) et retourne les éventuelles
     * ruptures. Une réponse avec {@code chaineIntegre = true} et
     * {@code maillonsRompus} vide indique une chaîne intacte.
     *
     * @param limiteLignes nombre maximum de maillons à vérifier ; {@code null}
     *                     pour vérifier toute la chaîne.
     */
    VerificationChaineResponse verifierChaineComplete(Long limiteLignes);

    /**
     * Vérifie un maillon isolé identifié par son hash complet ou préfixe
     * hexadécimal (utile pour saisir manuellement les 8 premiers caractères
     * affichés dans l'UI).
     *
     * @return {@link Optional#empty()} si aucun maillon ne correspond au hash.
     */
    Optional<VerificationMaillonResponse> verifierMaillonParHash(String hash);
}
