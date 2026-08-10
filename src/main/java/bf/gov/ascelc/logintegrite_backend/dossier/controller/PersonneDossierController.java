package bf.gov.ascelc.logintegrite_backend.dossier.controller;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.PersonneDossierCompletResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.service.PersonneDossierQueryService;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationFaitResumeResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/personnes/{personneId}")
@RequiredArgsConstructor
public class PersonneDossierController {

    private final PersonneDossierQueryService service;

    @GetMapping("/dossier-complet")
    public PersonneDossierCompletResponse dossierComplet(@PathVariable UUID personneId) {
        return service.obtenirPourPersonne(personneId);
    }
    
    @GetMapping("/implication-faits")
    public List<ImplicationFaitResumeResponse> listerImplicationFaits(@PathVariable UUID personneId) {
    return service.listerImplicationFaitsPourPersonne(personneId);
}
}
