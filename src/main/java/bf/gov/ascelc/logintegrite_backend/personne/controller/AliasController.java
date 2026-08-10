package bf.gov.ascelc.logintegrite_backend.personne.controller;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.AliasRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.AliasResponse;
import bf.gov.ascelc.logintegrite_backend.personne.service.AliasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AliasController {

    private final AliasService service;

    @GetMapping("/api/v1/personnes/{personneId}/alias")
    public List<AliasResponse> lister(@PathVariable UUID personneId) {
        return service.listerParPersonne(personneId);
    }

    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping("/api/v1/personnes/{personneId}/alias")
    @ResponseStatus(HttpStatus.CREATED)
    public AliasResponse creer(@PathVariable UUID personneId, @Valid @RequestBody AliasRequest request) {
        return service.creer(personneId, request);
    }

    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @DeleteMapping("/api/v1/alias/{aliasId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID aliasId) {
        service.supprimer(aliasId);
    }
}
