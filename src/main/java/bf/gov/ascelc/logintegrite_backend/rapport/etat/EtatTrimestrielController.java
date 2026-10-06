package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.rapport.etat.EtatTrimestrielRepository.EtatTrimestrielResume;
import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rapports/etats-trimestriels")
@PreAuthorize("hasAnyRole('VALIDATEUR','ADMIN')")
@RequiredArgsConstructor
public class EtatTrimestrielController {

    private final EtatTrimestrielRepository repository;
    private final EtatTrimestrielService service;
    private final CurrentUserProvider utilisateurCourant;

    @GetMapping
    public List<EtatTrimestrielResume> lister() {
        return repository.lister();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
        byte[] pdf = repository.pdf(id).orElseThrow(() -> new ResourceNotFoundException("Etat trimestriel", id));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"etat-trimestriel-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> excel(@PathVariable UUID id) {
        byte[] excel = repository.excel(id).orElseThrow(() -> new ResourceNotFoundException("Etat trimestriel", id));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"etat-trimestriel-" + id + ".xlsx\"")
                .body(excel);
    }

    @PostMapping("/generer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> generer(@RequestParam int annee, @RequestParam int trimestre) {
        PeriodeTrimestre periode = new PeriodeTrimestre(annee, trimestre);
        if (periode.fin().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Le trimestre " + periode.libelle() + " n'est pas encore termine.");
        }
        Utilisateur u = utilisateurCourant.utilisateurCourant();
        service.generer(periode, u.getPrenom() + " " + u.getNom(), LocalDate.now());
        return ResponseEntity.noContent().build();
    }
}
