package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import bf.gov.ascelc.logintegrite_backend.mapper.PersonnePhysiqueMapper;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiURLs.PERSONNES_PHYSIQUES) // Aligné dynamiquement sur "/api/v1/personnes-physiques"
@RequiredArgsConstructor
public class PersonnePhysiqueController {

    private final PersonnePhysiqueService ppService;
    private final PersonnePhysiqueMapper ppMapper;

    @PostMapping
    public ResponseEntity<PersonnePhysiqueResponse> creer(@Valid @RequestBody PersonnePhysiqueRequest request) {
        PersonnePhysique pp = ppMapper.toEntity(request);
        PersonnePhysique nouvelle = ppService.creer(pp);
        return ResponseEntity.ok(ppMapper.toResponse(nouvelle));
    }

    @GetMapping(ApiURLs.PERSONNES_PHYSIQUES_RECHERCHE)
    // Sécurisation stricte alignée sur les jetons Keycloak
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR', 'ROLE_public')")
    public ResponseEntity<?> lister(@AuthenticationPrincipal Jwt jwt) {
        List<PersonnePhysique> listeBrute = ppService.listerTout();

        List<String> roles = Collections.emptyList();
        if (jwt != null && jwt.hasClaim("realm_access")) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess.containsKey("roles")) {
                roles = ((List<?>) realmAccess.get("roles")).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
        }

        // Cohérence avec les rôles globaux de Log Intégrité
        boolean isPublicOnly = roles.contains("public")
                && !roles.contains("ADMINISTRATEUR")
                && !roles.contains("AGENT")
                && !roles.contains("VALIDATEUR");

        if (isPublicOnly) {
            return ResponseEntity.ok(ppMapper.toPublicResponseList(listeBrute));
        }

        // Accès institutionnel complet
        List<PersonnePhysiqueResponse> responses = listeBrute.stream()
                .map(ppMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonnePhysiqueResponse> consulter(@PathVariable UUID id) {
        PersonnePhysique pp = ppService.consulter(id);
        return ResponseEntity.ok(ppMapper.toResponse(pp));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonnePhysiqueResponse> modifier(@PathVariable UUID id, @Valid @RequestBody PersonnePhysiqueRequest request) {
        PersonnePhysique ppExistante = ppService.consulter(id);
        ppMapper.updateEntityFromRequest(request, ppExistante);
        PersonnePhysique modifiee = ppService.modifier(id, ppExistante);
        return ResponseEntity.ok(ppMapper.toResponse(modifiee));
    }

    @PutMapping("/{id}/soumettre")
    public ResponseEntity<PersonnePhysiqueResponse> soumettre(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonnePhysique pp = (PersonnePhysique) ppService.soumettre(id, agentId);
        return ResponseEntity.ok(ppMapper.toResponse(pp));
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<PersonnePhysiqueResponse> valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonnePhysique pp = (PersonnePhysique) ppService.valider(id, validateurId);
        return ResponseEntity.ok(ppMapper.toResponse(pp));
    }

    @PutMapping("/{id}/rejeter")
    public ResponseEntity<PersonnePhysiqueResponse> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonnePhysique pp = (PersonnePhysique) ppService.rejeter(id, motif, validateurId);
        return ResponseEntity.ok(ppMapper.toResponse(pp));
    }

    @DeleteMapping("/{id}/archiver")
    public ResponseEntity<Void> archiver(@PathVariable UUID id) {
        ppService.archiver(id);
        return ResponseEntity.noContent().build();
    }
}