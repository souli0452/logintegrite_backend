package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatistiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.StatistiqueResponse;
import bf.gov.ascelc.logintegrite_backend.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs.*;

@RestController
@RequestMapping(STATISTIQUES)
@RequiredArgsConstructor
public class StatistiqueController {

    private final StatistiqueService statistiqueService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','VALIDATEUR','AGENT')")
    public ResponseEntity<StatistiqueResponse> tableauDeBord(StatistiqueRequest filtres) {
        // On passe l'objet de requête contenant les filtres optionnels au service
        return ResponseEntity.ok(statistiqueService.calculer(filtres));
    }
}