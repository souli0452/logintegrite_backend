package bf.gov.ascelc.logintegrite_backend.referentiel.controller;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypeDocumentRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypeDocumentResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.TypeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referentiels/types-document")
@RequiredArgsConstructor
public class TypeDocumentController {

    private final TypeDocumentService service;

    @Operation(summary = "Lister tous les types de document")
    @GetMapping
    public List<TypeDocumentResponse> lister() {
        return service.lister();
    }

    @Operation(summary = "Creer un nouveau type de document")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TypeDocumentResponse creer(@Valid @RequestBody TypeDocumentRequest request) {
        return service.creer(request);
    }

    @Operation(summary = "Modifier un type de document existant")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public TypeDocumentResponse modifier(@PathVariable UUID id, @Valid @RequestBody TypeDocumentRequest request) {
        return service.modifier(id, request);
    }

    @Operation(summary = "Supprimer un type de document")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
