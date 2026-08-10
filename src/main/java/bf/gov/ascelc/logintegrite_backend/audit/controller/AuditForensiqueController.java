package bf.gov.ascelc.logintegrite_backend.audit.controller;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.EtatChaineResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.KpiForensiqueResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.VerificationChaineResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.VerificationMaillonResponse;
import bf.gov.ascelc.logintegrite_backend.audit.service.AuditIntegriteService;
import bf.gov.ascelc.logintegrite_backend.audit.service.AuditStatistiquesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints REST du volet forensique de l'audit :
 * <ul>
 *   <li>bandeau d'intégrité (état de la chaîne) ;</li>
 *   <li>vérification cryptographique complète ou ciblée ;</li>
 *   <li>KPI forensiques (4 cartes du haut d'écran).</li>
 * </ul>
 *
 * <p>L'accès est restreint au rôle {@code ADMIN} conformément au CDC — seul un
 * administrateur peut consulter la piste d'audit.
 *
 * <p>Le contrôleur historique {@link AuditQueryController} reste en place pour
 * la compatibilité de l'écran actuel (tableau paginé). Les nouveaux endpoints
 * sont montés sous {@code /api/v1/audit/forensique/*}.
 */
@RestController
@RequestMapping("/api/v1/audit/forensique")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit — Forensique",
     description = "Vérification cryptographique de la chaîne d'audit et KPI forensiques.")
public class AuditForensiqueController {

    private final AuditIntegriteService integriteService;
    private final AuditStatistiquesService statistiquesService;

    // -------------------------------------------------------------------------
    // 1. Bandeau d'intégrité
    // -------------------------------------------------------------------------
    @GetMapping("/etat-chaine")
    @Operation(summary = "Instantané de l'état de la chaîne d'audit",
               description = "Total d'entrées scellées, dernier hash, plages temporelles, cardinalités. " +
                             "Requête O(1), aucune vérification cryptographique déclenchée.")
    public EtatChaineResponse etatChaine() {
        return integriteService.etatChaine();
    }

    // -------------------------------------------------------------------------
    // 2. Vérification cryptographique complète
    // -------------------------------------------------------------------------
    @PostMapping("/verifier-chaine")
    @Operation(summary = "Recalcule tous les hashs et détecte les ruptures",
               description = "Opération coûteuse (O(N)) — à déclencher à la demande depuis le bouton " +
                             "\"Vérifier l'intégrité maintenant\". La limite optionnelle permet de vérifier " +
                             "un échantillon plutôt que la totalité, utile en démonstration ou en début de projet.")
    public VerificationChaineResponse verifierChaineComplete(
            @RequestParam(required = false)
            @Min(value = 1, message = "La limite doit être positive.")
            @Max(value = 10_000_000L, message = "Limite maximale : 10 millions de maillons.")
            Long limiteLignes) {
        return integriteService.verifierChaineComplete(limiteLignes);
    }

    // -------------------------------------------------------------------------
    // 3. Vérification d'un maillon isolé
    // -------------------------------------------------------------------------
    @GetMapping("/verifier-maillon")
    @Operation(summary = "Localise un maillon par son hash et vérifie son intégrité",
               description = "Accepte un hash complet ou un préfixe (8+ caractères hexadécimaux). " +
                             "Répond 404 si aucun maillon ne correspond.")
    public ResponseEntity<VerificationMaillonResponse> verifierMaillon(
            @RequestParam @NotBlank(message = "Le hash à vérifier est obligatoire.") String hash) {
        return integriteService.verifierMaillonParHash(hash)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -------------------------------------------------------------------------
    // 4. KPI forensiques du haut d'écran
    // -------------------------------------------------------------------------
    @GetMapping("/kpi")
    @Operation(summary = "KPI forensiques agrégés (4 cartes du haut d'écran)",
               description = "Actions du jour (ventilées + delta), consultations 24h, utilisateurs actifs, " +
                             "alertes sécurité. Les paramètres d'horaire ouvrable configurent la détection " +
                             "des actions hors norme.")
    public KpiForensiqueResponse kpiForensique(
            @RequestParam(defaultValue = "7")
            @Min(value = 0, message = "L'heure d'ouverture est comprise entre 0 et 23.")
            @Max(value = 23, message = "L'heure d'ouverture est comprise entre 0 et 23.")
            int heureOuverture,

            @RequestParam(defaultValue = "18")
            @Min(value = 0, message = "L'heure de fermeture est comprise entre 0 et 23.")
            @Max(value = 23, message = "L'heure de fermeture est comprise entre 0 et 23.")
            int heureFermeture) {
        return statistiquesService.kpiForensique(heureOuverture, heureFermeture);
    }
}
