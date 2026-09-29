// document/controller/DocumentController.java
package bf.gov.ascelc.logintegrite_backend.document.controller;

import bf.gov.ascelc.logintegrite_backend.document.dto.request.DocumentRequest;
import bf.gov.ascelc.logintegrite_backend.document.dto.response.DocumentResponse;
import bf.gov.ascelc.logintegrite_backend.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;

@PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
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
        if (!dossierId.equals(meta.getDossierId())) {
            // Le document n'appartient pas au dossier de l'URL : on ne revele pas son existence.
            throw new ResourceNotFoundException("Document", documentId);
        }
        Resource fichier = service.telechargerFichier(documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getTypeMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(meta.getNomOriginal(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(fichier);
    }
}
