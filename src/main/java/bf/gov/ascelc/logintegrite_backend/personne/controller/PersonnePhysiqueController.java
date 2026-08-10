// personne/controller/PersonnePhysiqueController.java
package bf.gov.ascelc.logintegrite_backend.personne.controller;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.personne.service.PersonnePhysiqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/personnes/physiques")
@RequiredArgsConstructor
public class PersonnePhysiqueController {

    private final PersonnePhysiqueService service;

    @GetMapping
    public Page<PersonnePhysiqueResponse> lister(Pageable pageable) { return service.lister(pageable); }

    @GetMapping("/{id}")
    public PersonnePhysiqueResponse obtenir(@PathVariable UUID id) { return service.obtenir(id); }
    
    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonnePhysiqueResponse creer(@Valid @RequestBody PersonnePhysiqueRequest request) {
        return service.creer(request);
    }
    
     @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    @PutMapping("/{id}")
    public PersonnePhysiqueResponse modifier(@PathVariable UUID id, @Valid @RequestBody PersonnePhysiqueRequest request) {
        return service.modifier(id, request);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) { service.supprimer(id); }
}
