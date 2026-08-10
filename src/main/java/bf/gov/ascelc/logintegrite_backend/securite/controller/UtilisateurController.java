package bf.gov.ascelc.logintegrite_backend.securite.controller;

import bf.gov.ascelc.logintegrite_backend.securite.dto.request.UtilisateurCreationRequest;
import bf.gov.ascelc.logintegrite_backend.securite.dto.response.UtilisateurResponse;
import bf.gov.ascelc.logintegrite_backend.securite.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService service;

    @GetMapping
    public List<UtilisateurResponse> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public UtilisateurResponse obtenir(@PathVariable UUID id) {
        return service.obtenir(id);
    }

    // NOUVEAU : creation d'un utilisateur (Keycloak + local)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurResponse creer(@Valid @RequestBody UtilisateurCreationRequest request) {
        return service.creer(request);
    }

    // NOUVEAU : activer/desactiver
    @PatchMapping("/{id}/activation")
    public UtilisateurResponse modifierActivation(@PathVariable UUID id,
                                                    @RequestBody Map<String, Boolean> body) {
        boolean actif = body.getOrDefault("actif", true);
        return service.modifierActivation(id, actif);
    }

    // NOUVEAU : suppression complete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }

    @PostMapping("/{id}/roles/{roleHabilitationId}")
    public UtilisateurResponse attribuerRole(@PathVariable UUID id,
                                              @PathVariable UUID roleHabilitationId) {
        return service.attribuerRole(id, roleHabilitationId);
    }

    @DeleteMapping("/{id}/roles/{roleHabilitationId}")
    public UtilisateurResponse retirerRole(@PathVariable UUID id,
                                            @PathVariable UUID roleHabilitationId) {
        return service.retirerRole(id, roleHabilitationId);
    }
}
