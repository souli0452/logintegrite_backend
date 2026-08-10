// document/controller/DocumentController.java
package bf.gov.ascelc.logintegrite_backend.document.controller;

import bf.gov.ascelc.logintegrite_backend.document.dto.request.DocumentRequest;
import bf.gov.ascelc.logintegrite_backend.document.dto.response.DocumentResponse;
import bf.gov.ascelc.logintegrite_backend.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/dossiers/{dossierId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    @GetMapping
    public List<DocumentResponse> lister(@PathVariable UUID dossierId) {
        return service.listerParDossier(dossierId);
    }
    
    @Operation(summary = "Deposer un document dans un dossier",
           description = "Upload multipart. Le hash SHA-256 est calcule automatiquement et rendu immuable.")
    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse deposer(@PathVariable UUID dossierId, @Valid @ModelAttribute DocumentRequest request) {
        return service.deposer(dossierId, request);
    }

    @GetMapping("/{documentId}/telecharger")
    public ResponseEntity<Resource> telecharger(@PathVariable UUID dossierId, @PathVariable UUID documentId) {
        DocumentResponse meta = service.obtenir(documentId);
        Resource fichier = service.telechargerFichier(documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getTypeMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getNomOriginal() + "\"")
                .body(fichier);
    }
}
