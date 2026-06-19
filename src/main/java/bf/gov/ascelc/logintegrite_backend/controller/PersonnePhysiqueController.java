package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import bf.gov.ascelc.logintegrite_backend.mapper.PersonnePhysiqueMapper;
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
@RequestMapping(ApiURLs.PERSONNES_PHYSIQUES)
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
    public ResponseEntity<List<PersonnePhysiqueResponse>> lister() {
        List<PersonnePhysiqueResponse> responses = ppService.listerTout().stream()
                .map(ppMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping(ApiURLs.FICHES_ID)
    public ResponseEntity<PersonnePhysiqueResponse> consulter(@PathVariable UUID id) {
        PersonnePhysique pp = ppService.consulter(id);
        return ResponseEntity.ok(ppMapper.toResponse(pp));
    }

    @PutMapping(ApiURLs.FICHES_ID)
    public ResponseEntity<PersonnePhysiqueResponse> modifier(@PathVariable UUID id, @Valid @RequestBody PersonnePhysiqueRequest request) {
        PersonnePhysique ppExistante = ppService.consulter(id);
        ppMapper.updateEntityFromRequest(request, ppExistante);
        PersonnePhysique modifiee = ppService.modifier(id, ppExistante);
        return ResponseEntity.ok(ppMapper.toResponse(modifiee));
    }

    @PutMapping(ApiURLs.FICHES_SOUMETTRE)
    public ResponseEntity<PersonnePhysiqueResponse> soumettre(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonnePhysique pp = (PersonnePhysique) ppService.soumettre(id, agentId);
        return ResponseEntity.ok(ppMapper.toResponse(pp));
    }

    @PutMapping(ApiURLs.FICHES_VALIDER)
    public ResponseEntity<PersonnePhysiqueResponse> valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonnePhysique pp = (PersonnePhysique) ppService.valider(id, validateurId);
        return ResponseEntity.ok(ppMapper.toResponse(pp));
    }

    @PutMapping(ApiURLs.FICHES_REJETER)
    public ResponseEntity<PersonnePhysiqueResponse> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonnePhysique pp = (PersonnePhysique) ppService.rejeter(id, motif, validateurId);
        return ResponseEntity.ok(ppMapper.toResponse(pp));
    }

    @DeleteMapping(ApiURLs.FICHES_ARCHIVER)
    public ResponseEntity<Void> archiver(@PathVariable UUID id) {
        ppService.archiver(id);
        return ResponseEntity.noContent().build();
    }
}