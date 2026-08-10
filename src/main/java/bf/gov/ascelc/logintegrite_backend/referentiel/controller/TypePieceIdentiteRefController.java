package bf.gov.ascelc.logintegrite_backend.referentiel.controller;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypePieceIdentiteRefRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypePieceIdentiteRefResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.TypePieceIdentiteRefService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/types-piece-identite")
@RequiredArgsConstructor
public class TypePieceIdentiteRefController {

    private final TypePieceIdentiteRefService service;

    @GetMapping
    public List<TypePieceIdentiteRefResponse> lister() {
        return service.lister();
    }

    @GetMapping("/actifs")
    public List<TypePieceIdentiteRefResponse> listerActifs() {
        return service.listerActifs();
    }

    @GetMapping("/{id}")
    public TypePieceIdentiteRefResponse obtenir(@PathVariable UUID id) {
        return service.obtenir(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TypePieceIdentiteRefResponse> creer(
            @Valid @RequestBody TypePieceIdentiteRefRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TypePieceIdentiteRefResponse modifier(@PathVariable UUID id,
                                                  @Valid @RequestBody TypePieceIdentiteRefRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
