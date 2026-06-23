package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.response.ReferentielResponse;
import bf.gov.ascelc.logintegrite_backend.repository.EntiteOrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs.*;

@RestController
@RequestMapping(REFERENTIEL) // Dynamiquement aligné sur "/api/v1/referentiel"
@RequiredArgsConstructor
public class ReferentielController {

    private final EntiteOrganisationRepository entiteRepo;

    @GetMapping(REFERENTIEL_ENTITES) // Équivaut à "/entites" -> URL finale: /api/v1/referentiel/entites
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReferentielResponse>> entites() {
        List<ReferentielResponse> responses = entiteRepo.findByActifTrue().stream()
                .map(entite -> new ReferentielResponse(
                        entite.getId(),
                        entite.getCode() + " - " + entite.getNom()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}