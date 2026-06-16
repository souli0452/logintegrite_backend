package bf.gov.ascelc.logintegrite_backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FicheMiseEnCauseResponse {
    private UUID id;
    private String typeFiche;        // "PP" ou "PM"
    private String cibleNom;         // Nom complet (PP) ou Raison Sociale (PM)
    private String identifiantUnique; // Matricule (PP) ou IFU (PM)
    private String entiteNom;
    private String regionNom;
    private String statutFiche;
    private String statutJudiciaire;
    private LocalDateTime dateModification;

    // La méthode statique fromEntity a été entièrement retirée ici
}