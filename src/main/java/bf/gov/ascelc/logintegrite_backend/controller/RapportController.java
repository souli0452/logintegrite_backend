package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.response.FicheExportResponse;
import bf.gov.ascelc.logintegrite_backend.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs.*;

@RestController
@RequestMapping(RAPPORTS) // "/api/v1/rapports"
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class RapportController {

    private final RapportService rapportService;

    @GetMapping(RAPPORTS_PDF) // "/pdf"
    public ResponseEntity<byte[]> exporterPDF(@RequestParam(defaultValue = "Rapport des Mises en Cause") String titre) throws IOException {
        // On récupère une liste de DTOs propres construits par le service
        List<FicheExportResponse> fiches = rapportService.getFichesActivesPourExport();
        byte[] pdfContenu = rapportService.genererPDF(titre, fiches);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContenu);
    }

    @GetMapping(RAPPORTS_EXCEL) // "/excel"
    public ResponseEntity<byte[]> exporterExcel(@RequestParam(defaultValue = "Export Log Intégrité") String titre) throws IOException {
        List<FicheExportResponse> fiches = rapportService.getFichesActivesPourExport();
        byte[] excelContenu = rapportService.genererExcel(titre, fiches);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContenu);
    }
}