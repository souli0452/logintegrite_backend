// dossier/controller/PeineController.java
package bf.gov.ascelc.logintegrite_backend.dossier.controller;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.PeineRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.PeineResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.service.PeineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/liaisons-faits/{implicationFaitId}/peines")
@RequiredArgsConstructor
public class PeineController {

    private final PeineService service;

    @GetMapping
    public List<PeineResponse> lister(@PathVariable UUID implicationFaitId) {
        return service.listerParImplicationFait(implicationFaitId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PeineResponse creer(@PathVariable UUID implicationFaitId, @Valid @RequestBody PeineRequest request) {
        return service.creer(implicationFaitId, request);
    }
}
