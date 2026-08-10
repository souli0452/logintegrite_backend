package bf.gov.ascelc.logintegrite_backend.parametresysteme.controller;

import bf.gov.ascelc.logintegrite_backend.parametresysteme.dto.request.ParametreSystemeRequest;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.dto.response.ParametreSystemeResponse;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.service.ParametreSystemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Reglages applicatifs internes (seuils, feature flags) : reserve a l'ADMIN
// en lecture ET en ecriture, contrairement aux referentiels metier qui sont
// lisibles par tous les utilisateurs authentifies.
@RestController
@RequestMapping("/api/v1/parametres-systeme")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ParametreSystemeController {

    private final ParametreSystemeService service;

    @GetMapping
    public List<ParametreSystemeResponse> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public ParametreSystemeResponse obtenir(@PathVariable UUID id) {
        return service.obtenir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParametreSystemeResponse creer(@Valid @RequestBody ParametreSystemeRequest request) {
        return service.creer(request);
    }

    @PutMapping("/{id}")
    public ParametreSystemeResponse modifier(@PathVariable UUID id, @Valid @RequestBody ParametreSystemeRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
