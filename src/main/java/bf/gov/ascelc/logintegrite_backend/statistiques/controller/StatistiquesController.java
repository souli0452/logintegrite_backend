package bf.gov.ascelc.logintegrite_backend.statistiques.controller;

import bf.gov.ascelc.logintegrite_backend.statistiques.dto.response.DashboardExecutifResponse;
import bf.gov.ascelc.logintegrite_backend.statistiques.service.DashboardExecutifService;
// Gardez vos imports existants (StatistiqueService, StatistiquesGlobalesResponse) si votre controller les utilise deja
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistiques")
@RequiredArgsConstructor
public class StatistiquesController {

    private final DashboardExecutifService dashboardExecutifService;
    // gardez vos autres services existants ici

    @GetMapping("/dashboard-executif")
    public DashboardExecutifResponse dashboardExecutif() {
        return dashboardExecutifService.calculer();
    }

    // gardez vos autres endpoints existants ici, notamment /globales
}
