package bf.gov.ascelc.logintegrite_backend.referentiel.controller;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypeInfractionRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypeInfractionResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.TypeInfractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referentiels/types-infraction")
@RequiredArgsConstructor
public class TypeInfractionController {

    private final TypeInfractionService service;

    @GetMapping
    public List<TypeInfractionResponse> lister() { return service.lister(); }

    @GetMapping("/{id}")
    public TypeInfractionResponse obtenir(@PathVariable UUID id) { return service.obtenir(id); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TypeInfractionResponse creer(@Valid @RequestBody TypeInfractionRequest request) {
        return service.creer(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public TypeInfractionResponse modifier(@PathVariable UUID id, @Valid @RequestBody TypeInfractionRequest request) {
        return service.modifier(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) { service.supprimer(id); }
}
