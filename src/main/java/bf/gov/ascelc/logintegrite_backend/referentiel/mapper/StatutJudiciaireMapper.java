package bf.gov.ascelc.logintegrite_backend.referentiel.mapper;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.StatutJudiciaireResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.StatutJudiciaire;
import org.springframework.stereotype.Component;

@Component
public class StatutJudiciaireMapper {

    public StatutJudiciaire toEntity(StatutJudiciaireRequest request) {
        StatutJudiciaire entite = new StatutJudiciaire();
        entite.setLibelle(request.getLibelle());
        entite.setActif(request.isActif());
        return entite;
    }

    // Met a jour l'entite existante sans creer d'objet neuf
    public void mettreAJour(StatutJudiciaire entite, StatutJudiciaireRequest request) {
        entite.setLibelle(request.getLibelle());
        entite.setActif(request.isActif());
    }

    public StatutJudiciaireResponse toResponse(StatutJudiciaire entite) {
        return StatutJudiciaireResponse.builder()
                .id(entite.getId())
                .libelle(entite.getLibelle())
                .actif(entite.isActif())
                .build();
    }
}
