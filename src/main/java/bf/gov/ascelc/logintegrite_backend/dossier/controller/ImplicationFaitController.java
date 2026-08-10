// dossier/controller/ImplicationFaitController.java
package bf.gov.ascelc.logintegrite_backend.dossier.controller;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.ImplicationFaitRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.MiseAJourStatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationFaitResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.service.ImplicationFaitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImplicationFaitController {

    private final ImplicationFaitService service;

    @GetMapping("/api/v1/implications/{implicationId}/liaisons-faits")
    public List<ImplicationFaitResponse> lister(@PathVariable UUID implicationId) {
        return service.listerParImplication(implicationId);
    }

    @PostMapping("/api/v1/implications/{implicationId}/liaisons-faits")
    @ResponseStatus(HttpStatus.CREATED)
    public ImplicationFaitResponse creer(@PathVariable UUID implicationId, @Valid @RequestBody ImplicationFaitRequest request) {
        return service.creer(implicationId, request);
    }

    @PutMapping("/api/v1/liaisons-faits/{implicationFaitId}/statut")
    public ImplicationFaitResponse mettreAJourStatut(@PathVariable UUID implicationFaitId,
                                                       @Valid @RequestBody MiseAJourStatutJudiciaireRequest request) {
        return service.mettreAJourStatut(implicationFaitId, request);
    }
}
