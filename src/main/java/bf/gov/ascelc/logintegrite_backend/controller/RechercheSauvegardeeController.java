package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.RechercheSauvegardeeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.RechercheSauvegardeeResponse;
import bf.gov.ascelc.logintegrite_backend.service.RechercheSauvegardeeService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs; // Importation de tes constantes
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
// Racine : /api/v1/recherches-sauvegardees
@RequestMapping(ApiURLs.API_V1_ROOT + "/recherches-sauvegardees")
@RequiredArgsConstructor
public class RechercheSauvegardeeController {

    private final RechercheSauvegardeeService service;

    @PostMapping
    public ResponseEntity<RechercheSauvegardeeResponse> create(@Valid @RequestBody RechercheSauvegardeeRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RechercheSauvegardeeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RechercheSauvegardeeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RechercheSauvegardeeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<RechercheSauvegardeeResponse>> getMySearches(@AuthenticationPrincipal Jwt jwt) {
        // Extraction sécurisée de l'ID utilisateur Keycloak (le subject)
        String userId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(service.getMySearches(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}