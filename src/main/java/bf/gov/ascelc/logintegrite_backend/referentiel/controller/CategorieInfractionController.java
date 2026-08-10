package bf.gov.ascelc.logintegrite_backend.referentiel.controller;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.CategorieInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.CategorieInfractionResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.CategorieInfractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referentiels/categories-infraction")
@RequiredArgsConstructor
public class CategorieInfractionController {

    private final CategorieInfractionService service;

    @GetMapping
    public List<CategorieInfractionResponse> lister() { return service.lister(); }

    @GetMapping("/{id}")
    public CategorieInfractionResponse obtenir(@PathVariable UUID id) { return service.obtenir(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieInfractionResponse creer(@Valid @RequestBody CategorieInfractionRequest request) {
        return service.creer(request);
    }

    @PutMapping("/{id}")
    public CategorieInfractionResponse modifier(@PathVariable UUID id, @Valid @RequestBody CategorieInfractionRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) { service.supprimer(id); }
}
