package bf.gov.ascelc.logintegrite_backend.referentiel.mapper;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.SourceSignalementRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.SourceSignalementResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.SourceSignalement;
import org.springframework.stereotype.Component;

@Component
public class SourceSignalementMapper {

    public SourceSignalement toEntity(SourceSignalementRequest request) {
        SourceSignalement entite = new SourceSignalement();
        entite.setLibelle(request.getLibelle());
        entite.setDescription(request.getDescription());
        return entite;
    }

    public void mettreAJour(SourceSignalement entite, SourceSignalementRequest request) {
        entite.setLibelle(request.getLibelle());
        entite.setDescription(request.getDescription());
    }

    public SourceSignalementResponse toResponse(SourceSignalement entite) {
        return SourceSignalementResponse.builder()
                .id(entite.getId())
                .libelle(entite.getLibelle())
                .description(entite.getDescription())
                .build();
    }
}
