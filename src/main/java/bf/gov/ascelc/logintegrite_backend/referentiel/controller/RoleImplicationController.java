package bf.gov.ascelc.logintegrite_backend.referentiel.controller;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.RoleImplicationRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.RoleImplicationResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.RoleImplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referentiels/roles-implication")
@RequiredArgsConstructor
public class RoleImplicationController {

    private final RoleImplicationService service;

    @Operation(summary = "Lister tous les roles d'implication")
    @GetMapping
    public List<RoleImplicationResponse> lister() {
        return service.lister();
    }

    @Operation(summary = "Creer un nouveau role d'implication")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleImplicationResponse creer(@Valid @RequestBody RoleImplicationRequest request) {
        return service.creer(request);
    }

    @Operation(summary = "Modifier un role d'implication existant")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public RoleImplicationResponse modifier(@PathVariable UUID id, @Valid @RequestBody RoleImplicationRequest request) {
        return service.modifier(id, request);
    }

    @Operation(summary = "Supprimer un role d'implication")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
