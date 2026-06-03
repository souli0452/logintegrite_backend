package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FicheMiseEnCauseResponse {
    private Long id;
    private String typeFiche; // "PP" ou "PM"
    private String cibleNom;   // Nom complet (PP) ou Raison Sociale (PM)
    private String identifiantUnique; // Matricule (PP) ou IFU (PM)
    private String entiteNom;
    private String regionNom;
    private String statutFiche;
    private String statutJudiciaire;
    private LocalDateTime dateModification;

    public static FicheMiseEnCauseResponse fromEntity(FicheMiseEnCause f) {
        FicheMiseEnCauseResponse dto = new FicheMiseEnCauseResponse();
        dto.setId(f.getId());
        dto.setStatutFiche(f.getStatutFiche() != null ? f.getStatutFiche().name() : null);
        dto.setStatutJudiciaire(f.getStatutJudiciaire() != null ? f.getStatutJudiciaire().name() : null);
        dto.setDateModification(f.getDateModification());
        
        // Résolution propre du nom de l'entité
        dto.setEntiteNom(f.getEntite() != null ? f.getEntite().getNom() : null);
        dto.setRegionNom(f.getRegion() != null ? f.getRegion().getNom() : null);

        if (f instanceof PersonnePhysique pp) {
            dto.setTypeFiche("PP");
            dto.setCibleNom(pp.getNom() + " " + (pp.getPrenoms() != null ? pp.getPrenoms() : ""));
            dto.setIdentifiantUnique(pp.getMatricule());
        } else if (f instanceof PersonneMorale pm) {
            dto.setTypeFiche("PM");
            dto.setCibleNom(pm.getRaisonSociale());
            dto.setIdentifiantUnique(pm.getIfu());
        }
        return dto;
    }
}
