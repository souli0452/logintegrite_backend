// personne/controller/PersonneController.java
package bf.gov.ascelc.logintegrite_backend.personne.controller;

import bf.gov.ascelc.logintegrite_backend.document.dto.response.PersonneDocumentResponse;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneSearchCriteria;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonneResumeResponse;
import bf.gov.ascelc.logintegrite_backend.personne.enums.StatutAncrage;
import bf.gov.ascelc.logintegrite_backend.personne.service.PersonneService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/personnes")
@RequiredArgsConstructor
public class PersonneController {

    private final PersonneService service;

    @Operation(summary = "Recherche multi-critères des personnes",
               description = "Permet de rechercher des personnes avec filtres (y compris statutAncrage) et pagination.")
    @GetMapping("/recherche")
    public Page<PersonneResumeResponse> rechercher(
            @ModelAttribute PersonneSearchCriteria criteria,
            Pageable pageable) {
        return service.rechercher(criteria, pageable);
    }

    @GetMapping("/{id}")
    public PersonneResumeResponse obtenir(@PathVariable UUID id) {
        return service.obtenir(id);
    }

    @GetMapping("/{id}/historique-statuts")
    public List<Map<String, Object>> historiqueStatuts(@PathVariable UUID id) {
        return service.historiqueStatutsJudiciaires(id);
    }

    @GetMapping("/{id}/documents")
    public List<PersonneDocumentResponse> listerDocuments(@PathVariable UUID id) {
        return service.listerDocuments(id);
    }

    @Operation(summary = "Personnes en instruction",
               description = "Personnes n'ayant AUCUN dossier entièrement validé.")
    @GetMapping("/en-instruction")
    public Page<PersonneResumeResponse> listerEnInstruction(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PersonneSearchCriteria criteria = new PersonneSearchCriteria();
        criteria.setStatutAncrage(StatutAncrage.EN_INSTRUCTION);
        return service.rechercher(criteria, PageRequest.of(page, size));
    }

    @Operation(summary = "Personnes du registre officiel",
               description = "Personnes ayant AU MOINS un dossier entièrement validé (tous ses faits en VALIDEE).")
    @GetMapping("/registre-officiel")
    public Page<PersonneResumeResponse> listerRegistreOfficiel(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PersonneSearchCriteria criteria = new PersonneSearchCriteria();
        criteria.setStatutAncrage(StatutAncrage.REGISTRE_OFFICIEL);
        return service.rechercher(criteria, PageRequest.of(page, size));
    }
}
