// dossier/controller/ImplicationController.java
package bf.gov.ascelc.logintegrite_backend.dossier.controller;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.ImplicationRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.service.ImplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dossiers/{dossierId}/implications")
@RequiredArgsConstructor
public class ImplicationController {

    private final ImplicationService service;

    @GetMapping
    public List<ImplicationResponse> lister(@PathVariable UUID dossierId) {
        return service.listerParDossier(dossierId);
    }
    
    /** Retire une personne d'un dossier ouvert (voir la regle dans ImplicationService). */
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @DeleteMapping("/{implicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID dossierId, @PathVariable UUID implicationId) {
        service.supprimer(dossierId, implicationId);
    }

    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImplicationResponse creer(@PathVariable UUID dossierId, @Valid @RequestBody ImplicationRequest request) {
        return service.creer(dossierId, request);
    }
}
