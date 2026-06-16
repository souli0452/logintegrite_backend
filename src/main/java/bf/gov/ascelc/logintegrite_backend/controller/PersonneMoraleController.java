package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import bf.gov.ascelc.logintegrite_backend.mapper.CibleFicheMapper;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping(ApiURLs.PERSONNES_MORALES)
@RequiredArgsConstructor
public class PersonneMoraleController {

    private final PersonneMoraleService pmService;
    private final CibleFicheMapper cibleMapper;

    @PostMapping
    public ResponseEntity<PersonneMorale> creer(@Valid @RequestBody PersonneMoraleRequest request) {
        PersonneMorale pm = cibleMapper.toEntity(request);
        return ResponseEntity.ok(pmService.creer(pm));
    }

    @GetMapping(ApiURLs.FICHES_ID)
    public ResponseEntity<PersonneMorale> consulter(@PathVariable UUID id) {
        return ResponseEntity.ok((PersonneMorale) pmService.consulter(id));
    }

    @PutMapping(ApiURLs.FICHES_ID)
    public ResponseEntity<PersonneMorale> modifier(@PathVariable UUID id, @Valid @RequestBody PersonneMoraleRequest request) {
        PersonneMorale pmExistante = (PersonneMorale) pmService.consulter(id);
        cibleMapper.updateEntityFromRequest(request, pmExistante);
        return ResponseEntity.ok(pmService.modifier(id, pmExistante));
    }

    @PutMapping(ApiURLs.FICHES_SOUMETTRE)
    public ResponseEntity<FicheMiseEnCause> soumettre(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(pmService.soumettre(id, agentId));
    }

    @PutMapping(ApiURLs.FICHES_VALIDER)
    public ResponseEntity<FicheMiseEnCause> valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(pmService.valider(id, validateurId));
    }

    @PutMapping(ApiURLs.FICHES_REJETER)
    public ResponseEntity<FicheMiseEnCause> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(pmService.rejeter(id, motif, validateurId));
    }

    @DeleteMapping(ApiURLs.FICHES_ARCHIVER)
    public ResponseEntity<Void> archiver(@PathVariable UUID id) {
        pmService.archiver(id);
        return ResponseEntity.noContent().build();
    }
}