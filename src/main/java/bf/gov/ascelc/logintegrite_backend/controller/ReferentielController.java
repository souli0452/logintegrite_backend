package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.response.ReferentielResponse;
import bf.gov.ascelc.logintegrite_backend.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.repository.RegionRepository;
import bf.gov.ascelc.logintegrite_backend.repository.TypeInfractionRepository;
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
@RequestMapping(REFERENTIEL) // Équivaut à "/api/referentiel"
@RequiredArgsConstructor
public class ReferentielController {

    private final RegionRepository regionRepo;
    private final EntiteOrganisationRepository entiteRepo;
    private final TypeInfractionRepository typeRepo;

    @GetMapping(REFERENTIEL_REGIONS) // Équivaut à "/regions"
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReferentielResponse>> regions() {
        List<ReferentielResponse> responses = regionRepo.findByActifTrue().stream()
                .map(region -> new ReferentielResponse(region.getId(), region.getNom()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping(REFERENTIEL_ENTITES) // Équivaut à "/entites"
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

    @GetMapping(REFERENTIEL_TYPES_INFRACTION) // Équivaut à "/types-infraction"
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReferentielResponse>> types() {
        List<ReferentielResponse> responses = typeRepo.findAll().stream()
                .map(type -> new ReferentielResponse(type.getId(), type.getLibelle()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}