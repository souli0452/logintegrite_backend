package bf.gov.ascelc.logintegrite_backend.verification.controller;

import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Criteres;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Demande;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.DemandeRequest;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Fiche;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Resultat;
import bf.gov.ascelc.logintegrite_backend.verification.service.DemandeExportService;
import bf.gov.ascelc.logintegrite_backend.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * API de verification : la seule que peut appeler un compte de consultation seule
 * (voir AccesConsultantFilter). Tous les roles applicatifs peuvent l'utiliser.
 */
@RestController
@RequestMapping("/api/v1/verification")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','AGENT','VALIDATEUR','CONSULTANT')")
public class VerificationController {

    private final VerificationService service;
    private final DemandeExportService demandes;

    /** Recherche d'UNE personne precise : numero de piece, RCCM, IFU, numero de personne, ou nom + prenoms + date de naissance. */
    @GetMapping("/recherche")
    public List<Resultat> rechercher(
            @RequestParam(required = false) String numeroPiece,
            @RequestParam(required = false) String rccm,
            @RequestParam(required = false) String ifu,
            @RequestParam(required = false) String numeroPersonne,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenoms,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNaissance) {
        return service.rechercher(new Criteres(numeroPiece, rccm, ifu, numeroPersonne, nom, prenoms, dateNaissance));
    }

    @GetMapping("/personnes/{id}")
    public Fiche fiche(@PathVariable UUID id) {
        return service.obtenirFiche(id);
    }

    /** Demande, motivee, de l'export du dossier complet d'une personne. Accordee ou refusee par un administrateur. */
    @PostMapping("/personnes/{id}/demande-export")
    @ResponseStatus(HttpStatus.CREATED)
    public Demande demanderExport(@PathVariable UUID id, @RequestBody DemandeRequest requete) {
        return demandes.creer(id, requete.motif());
    }

    @GetMapping("/mes-demandes")
    public List<Demande> mesDemandes() {
        return demandes.mesDemandes();
    }
}
