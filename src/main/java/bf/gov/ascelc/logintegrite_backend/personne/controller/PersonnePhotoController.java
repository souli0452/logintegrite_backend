package bf.gov.ascelc.logintegrite_backend.personne.controller;

import bf.gov.ascelc.logintegrite_backend.personne.service.PersonnePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/personnes/{id}/photo")
@RequiredArgsConstructor
public class PersonnePhotoController {

    private final PersonnePhotoService service;

    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deposer(@PathVariable UUID id, @RequestParam("fichier") MultipartFile fichier) {
        service.deposer(id, fichier);
    }

    @GetMapping
    public ResponseEntity<Resource> obtenir(@PathVariable UUID id) {
        Resource fichier = service.recuperer(id);
        String typeMime = service.typeMime(id);
        return ResponseEntity.ok()
                .contentType(typeMime != null ? MediaType.parseMediaType(typeMime) : MediaType.APPLICATION_OCTET_STREAM)
                .body(fichier);
    }

    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
