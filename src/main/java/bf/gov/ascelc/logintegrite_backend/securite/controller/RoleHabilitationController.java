// securite/controller/RoleHabilitationController.java
package bf.gov.ascelc.logintegrite_backend.securite.controller;

import bf.gov.ascelc.logintegrite_backend.securite.dto.request.RoleHabilitationRequest;
import bf.gov.ascelc.logintegrite_backend.securite.dto.response.RoleHabilitationResponse;
import bf.gov.ascelc.logintegrite_backend.securite.service.RoleHabilitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles-habilitation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleHabilitationController {

    private final RoleHabilitationService service;

    @GetMapping
    public List<RoleHabilitationResponse> lister() { return service.lister(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleHabilitationResponse creer(@Valid @RequestBody RoleHabilitationRequest request) {
        return service.creer(request);
    }

    @PutMapping("/{id}")
    public RoleHabilitationResponse modifier(@PathVariable UUID id, @Valid @RequestBody RoleHabilitationRequest request) {
        return service.modifier(id, request);
    }
}
