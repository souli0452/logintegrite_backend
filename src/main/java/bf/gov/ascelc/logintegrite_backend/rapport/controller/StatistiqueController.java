// rapport/controller/StatistiqueController.java
package bf.gov.ascelc.logintegrite_backend.rapport.controller;

import bf.gov.ascelc.logintegrite_backend.rapport.dto.response.StatistiqueGlobaleResponse;
import bf.gov.ascelc.logintegrite_backend.rapport.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistiques")
@RequiredArgsConstructor
public class StatistiqueController {

    private final StatistiqueService service;

    @GetMapping("/globales")
    public StatistiqueGlobaleResponse globales() {
        return service.obtenirStatistiquesGlobales();
    }
}
