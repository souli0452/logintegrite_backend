package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.service.KeycloakAdminService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(ApiURLs.UTILISATEURS)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
public class UtilisateurController {

    private final KeycloakAdminService keycloakAdminService;

    // AJOUT : effectifs par rôle — alimente le widget "Aperçu" du dashboard
    // Admin (Top 5 entités / Statut judiciaire / Effectifs). Pur additif :
    // ne touche à aucun endpoint ni comportement existant.
    @GetMapping(ApiURLs.UTILISATEURS_EFFECTIFS)
    public ResponseEntity<Map<String, Integer>> effectifs() {
        return ResponseEntity.ok(Map.of(
                "AGENT", keycloakAdminService.listerIdsUtilisateursParRole("AGENT").size(),
                "VALIDATEUR", keycloakAdminService.listerIdsUtilisateursParRole("VALIDATEUR").size(),
                "ADMINISTRATEUR", keycloakAdminService.listerIdsUtilisateursParRole("ADMINISTRATEUR").size()
        ));
    }
}
