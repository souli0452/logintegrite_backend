package bf.gov.ascelc.logintegrite_backend.verification.controller;

import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.DecisionRequest;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Demande;
import bf.gov.ascelc.logintegrite_backend.verification.service.DemandeExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Traitement des demandes d'export par un administrateur (un compte de consultation ne peut pas appeler ces routes). */
@RestController
@RequestMapping("/api/v1/demandes-export")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DemandeExportAdminController {

    private final DemandeExportService demandes;

    @GetMapping
    public List<Demande> lister(@RequestParam(required = false) String statut) {
        return demandes.lister(statut);
    }

    @PutMapping("/{id}/decision")
    public Demande decider(@PathVariable UUID id, @RequestBody DecisionRequest requete) {
        return demandes.decider(id, requete.decision(), requete.commentaire());
    }
}
