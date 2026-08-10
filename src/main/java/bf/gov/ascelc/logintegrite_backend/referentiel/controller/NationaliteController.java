package bf.gov.ascelc.logintegrite_backend.referentiel.controller;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.NationaliteRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.NationaliteResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.NationaliteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nationalites")
@RequiredArgsConstructor
public class NationaliteController {

    private final NationaliteService service;

    /** Liste toutes les nationalités (accessible à tous les rôles authentifiés). */
    @GetMapping
    public List<NationaliteResponse> lister() {
        return service.lister();
    }

    /** Liste seulement les nationalités actives — utilisé par les formulaires. */
    @GetMapping("/actifs")
    public List<NationaliteResponse> listerActifs() {
        return service.listerActifs();
    }

    @GetMapping("/{id}")
    public NationaliteResponse obtenir(@PathVariable UUID id) {
        return service.obtenir(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NationaliteResponse> creer(@Valid @RequestBody NationaliteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public NationaliteResponse modifier(@PathVariable UUID id,
                                        @Valid @RequestBody NationaliteRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
