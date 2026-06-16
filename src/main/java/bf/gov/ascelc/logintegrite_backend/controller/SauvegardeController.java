package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.entity.Sauvegarde;
import bf.gov.ascelc.logintegrite_backend.repository.SauvegardeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
// On utilise une route cohérente avec ton API_ROOT ("/api/sauvegardes")
@RequestMapping("/api/sauvegardes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')") // Sécurité globale au niveau de la classe
public class SauvegardeController {

    private final SauvegardeRepository sauvegardeRepo;

    @GetMapping
    public ResponseEntity<List<Sauvegarde>> listerToutes() {
        // Retourne l'historique des sauvegardes triées de la plus récente à la plus ancienne
        return ResponseEntity.ok(sauvegardeRepo.findAllByOrderByDateDebutDesc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sauvegarde> consulter(@PathVariable UUID id) {
        return sauvegardeRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}