// personne/controller/PersonneMoraleController.java
package bf.gov.ascelc.logintegrite_backend.personne.controller;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.personne.service.PersonneMoraleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/personnes/morales")
@RequiredArgsConstructor
public class PersonneMoraleController {

    private final PersonneMoraleService service;

    @GetMapping
    public Page<PersonneMoraleResponse> lister(Pageable pageable) { return service.lister(pageable); }

    @GetMapping("/{id}")
    public PersonneMoraleResponse obtenir(@PathVariable UUID id) { return service.obtenir(id); }
    
    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonneMoraleResponse creer(@Valid @RequestBody PersonneMoraleRequest request) {
        return service.creer(request);
    }
     @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @PutMapping("/{id}")
    public PersonneMoraleResponse modifier(@PathVariable UUID id, @Valid @RequestBody PersonneMoraleRequest request) {
        return service.modifier(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) { service.supprimer(id); }
}
