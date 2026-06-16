package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiURLs.PERSONNES_PHYSIQUES) // "/api/fiches/personnes-physiques"
@RequiredArgsConstructor
public class PersonnePhysiqueController {

    private final PersonnePhysiqueService ppService;

    @PostMapping
    public ResponseEntity<PersonnePhysique> creer(@RequestBody PersonnePhysique pp) {
        return ResponseEntity.ok(ppService.creer(pp));
    }

    @GetMapping(ApiURLs.FICHES_ID) // "/{id}"
    public ResponseEntity<PersonnePhysique> consulter(@PathVariable UUID id) {
        return ResponseEntity.ok((PersonnePhysique) ppService.consulter(id));
    }

    @PutMapping(ApiURLs.FICHES_ID) // "/{id}"
    public ResponseEntity<PersonnePhysique> modifier(@PathVariable UUID id, @RequestBody PersonnePhysique pp) {
        return ResponseEntity.ok(ppService.modifier(id, pp));
    }

    @PutMapping(ApiURLs.FICHES_SOUMETTRE) // "/{id}/soumettre"
    public ResponseEntity<FicheMiseEnCause> soumettre(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(ppService.soumettre(id, agentId));
    }

    @PutMapping(ApiURLs.FICHES_VALIDER) // "/{id}/valider"
    public ResponseEntity<FicheMiseEnCause> valider(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(ppService.valider(id, validateurId));
    }

    @PutMapping(ApiURLs.FICHES_REJETER) // "/{id}/rejeter"
    public ResponseEntity<FicheMiseEnCause> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(ppService.rejeter(id, motif, validateurId));
    }

    @DeleteMapping(ApiURLs.FICHES_ARCHIVER) // "/{id}/archiver"
    public ResponseEntity<Void> archiver(@PathVariable UUID id) {
        ppService.archiver(id);
        return ResponseEntity.noContent().build();
    }
}