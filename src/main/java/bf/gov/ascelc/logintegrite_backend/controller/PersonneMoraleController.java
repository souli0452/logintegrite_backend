package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import bf.gov.ascelc.logintegrite_backend.mapper.PersonneMoraleMapper;
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
@RequestMapping(ApiURLs.PERSONNES_MORALES) // Aligné dynamiquement sur "/api/v1/personnes-morales"
@RequiredArgsConstructor
public class PersonneMoraleController {

    private final PersonneMoraleService pmService;
    private final PersonneMoraleMapper pmMapper;

    @PostMapping
    public ResponseEntity<PersonneMoraleResponse> creer(@Valid @RequestBody PersonneMoraleRequest request) {
        PersonneMorale pm = pmMapper.toEntity(request);
        PersonneMorale nouvelle = pmService.creer(pm);
        return ResponseEntity.ok(pmMapper.toResponse(nouvelle));
    }

    @GetMapping(ApiURLs.PERSONNES_MORALES_RECHERCHE)
    // Utilisation de hasAnyAuthority avec le nom brut et complet des rôles Keycloak pour éviter l'Access Denied
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR', 'ROLE_public')")
    public ResponseEntity<?> lister(@AuthenticationPrincipal Jwt jwt) {
        List<PersonneMorale> listeBrute = pmService.listerTout();

        List<String> roles = Collections.emptyList();
        if (jwt != null && jwt.hasClaim("realm_access")) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess.containsKey("roles")) {
                roles = ((List<?>) realmAccess.get("roles")).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
        }

        // Alignement des vérifications internes sur les rôles Keycloak de ton environnement
        boolean isPublicOnly = roles.contains("public")
                && !roles.contains("ADMINISTRATEUR")
                && !roles.contains("AGENT")
                && !roles.contains("VALIDATEUR");

        if (isPublicOnly) {
            return ResponseEntity.ok(pmMapper.toPublicResponseList(listeBrute));
        }

        List<PersonneMoraleResponse> responses = listeBrute.stream()
                .map(pmMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonneMoraleResponse> consulter(@PathVariable UUID id) {
        PersonneMorale pm = pmService.consulter(id);
        return ResponseEntity.ok(pmMapper.toResponse(pm));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonneMoraleResponse> modifier(@PathVariable UUID id, @Valid @RequestBody PersonneMoraleRequest request) {
        PersonneMorale pmExistante = pmService.consulter(id);
        pmMapper.updateEntityFromRequest(request, pmExistante);
        PersonneMorale modifiee = pmService.modifier(id, pmExistante);
        return ResponseEntity.ok(pmMapper.toResponse(modifiee));
    }

    @PutMapping("/{id}/soumettre")
    public ResponseEntity<PersonneMoraleResponse> soumettre(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonneMorale pm = (PersonneMorale) pmService.soumettre(id, agentId);
        return ResponseEntity.ok(pmMapper.toResponse(pm));
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<PersonneMoraleResponse> valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonneMorale pm = (PersonneMorale) pmService.valider(id, validateurId);
        return ResponseEntity.ok(pmMapper.toResponse(pm));
    }

    @PutMapping("/{id}/rejeter")
    public ResponseEntity<PersonneMoraleResponse> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonneMorale pm = (PersonneMorale) pmService.rejeter(id, motif, validateurId);
        return ResponseEntity.ok(pmMapper.toResponse(pm));
    }

    @DeleteMapping("/{id}/archiver")
    public ResponseEntity<Void> archiver(@PathVariable UUID id) {
        pmService.archiver(id);
        return ResponseEntity.noContent().build();
    }
}