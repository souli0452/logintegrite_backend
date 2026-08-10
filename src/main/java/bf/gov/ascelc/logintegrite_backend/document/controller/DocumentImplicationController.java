// document/controller/DocumentImplicationController.java
package bf.gov.ascelc.logintegrite_backend.document.controller;

import bf.gov.ascelc.logintegrite_backend.document.dto.response.DocumentResponse;
import bf.gov.ascelc.logintegrite_backend.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DocumentImplicationController {

    private final DocumentService service;

    @GetMapping("/api/v1/implications/{implicationId}/documents-visibles")
    public List<DocumentResponse> documentsVisibles(@PathVariable UUID implicationId) {
        return service.listerVisiblesPourImplication(implicationId);
    }

    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping("/api/v1/documents/{documentId}/implications/{implicationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void tagger(@PathVariable UUID documentId, @PathVariable UUID implicationId) {
        service.tagger(documentId, implicationId);
    }

    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @DeleteMapping("/api/v1/documents/{documentId}/implications/{implicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerTag(@PathVariable UUID documentId, @PathVariable UUID implicationId) {
        service.retirerTag(documentId, implicationId);
    }
}
