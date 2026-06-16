package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.entity.*;
import bf.gov.ascelc.logintegrite_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public ResponseEntity<List<Region>> regions() {
        return ResponseEntity.ok(regionRepo.findByActifTrue());
    }

    @GetMapping(REFERENTIEL_ENTITES) // Équivaut à "/entites"
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntiteOrganisation>> entites() {
        return ResponseEntity.ok(entiteRepo.findByActifTrue());
    }

    @GetMapping(REFERENTIEL_TYPES_INFRACTION) // Équivaut à "/types-infraction"
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TypeInfraction>> types() {
        return ResponseEntity.ok(typeRepo.findAll());
    }
}