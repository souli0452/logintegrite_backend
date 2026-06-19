package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import bf.gov.ascelc.logintegrite_backend.mapper.PersonneMoraleMapper;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiURLs.PERSONNES_MORALES)
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
    public ResponseEntity<List<PersonneMoraleResponse>> lister() {
        List<PersonneMoraleResponse> responses = pmService.listerTout().stream()
                .map(pmMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping(ApiURLs.FICHES_ID)
    public ResponseEntity<PersonneMoraleResponse> consulter(@PathVariable UUID id) {
        PersonneMorale pm = pmService.consulter(id);
        return ResponseEntity.ok(pmMapper.toResponse(pm));
    }

    @PutMapping(ApiURLs.FICHES_ID)
    public ResponseEntity<PersonneMoraleResponse> modifier(@PathVariable UUID id, @Valid @RequestBody PersonneMoraleRequest request) {
        PersonneMorale pmExistante = pmService.consulter(id);
        pmMapper.updateEntityFromRequest(request, pmExistante);
        PersonneMorale modifiee = pmService.modifier(id, pmExistante);
        return ResponseEntity.ok(pmMapper.toResponse(modifiee));
    }

    @PutMapping(ApiURLs.FICHES_SOUMETTRE)
    public ResponseEntity<PersonneMoraleResponse> soumettre(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonneMorale pm = (PersonneMorale) pmService.soumettre(id, agentId);
        return ResponseEntity.ok(pmMapper.toResponse(pm));
    }

    @PutMapping(ApiURLs.FICHES_VALIDER)
    public ResponseEntity<PersonneMoraleResponse> valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonneMorale pm = (PersonneMorale) pmService.valider(id, validateurId);
        return ResponseEntity.ok(pmMapper.toResponse(pm));
    }

    @PutMapping(ApiURLs.FICHES_REJETER)
    public ResponseEntity<PersonneMoraleResponse> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonneMorale pm = (PersonneMorale) pmService.rejeter(id, motif, validateurId);
        return ResponseEntity.ok(pmMapper.toResponse(pm));
    }

    @DeleteMapping(ApiURLs.FICHES_ARCHIVER)
    public ResponseEntity<Void> archiver(@PathVariable UUID id) {
        pmService.archiver(id);
        return ResponseEntity.noContent().build();
    }
}