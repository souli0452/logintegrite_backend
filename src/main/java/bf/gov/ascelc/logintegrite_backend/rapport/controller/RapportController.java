// rapport/controller/RapportController.java
package bf.gov.ascelc.logintegrite_backend.rapport.controller;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneSearchCriteria;
import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService service;

    @GetMapping("/dossiers/{dossierId}/pdf")
    public ResponseEntity<byte[]> pdfDossier(@PathVariable UUID dossierId) {
        byte[] pdf = service.genererPdfDossier(dossierId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dossier-" + dossierId + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/personnes/excel")
    public ResponseEntity<byte[]> excelRecherchePersonnes(@ModelAttribute PersonneSearchCriteria criteria) {
        byte[] excel = service.genererExcelRecherchePersonnes(criteria);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recherche-personnes.xlsx\"")
                .body(excel);
    }
    
    @GetMapping("/registre-officiel/pdf")
public ResponseEntity<byte[]> pdfRegistreOfficiel() {
    byte[] pdf = service.genererPdfRegistreOfficiel();
    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"registre-officiel-" + java.time.LocalDate.now() + ".pdf\"")
            .body(pdf);
}

@GetMapping("/dossiers/excel")
public ResponseEntity<byte[]> excelDossiers() {
    byte[] excel = service.genererExcelDossiers();
    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"dossiers-" + java.time.LocalDate.now() + ".xlsx\"")
            .body(excel);
}
}
