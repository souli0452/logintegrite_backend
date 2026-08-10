package bf.gov.ascelc.logintegrite_backend.referentiel.mapper;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.RoleImplicationRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.RoleImplicationResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.RoleImplication;
import org.springframework.stereotype.Component;

@Component
public class RoleImplicationMapper {

    public RoleImplication toEntity(RoleImplicationRequest request) {
        RoleImplication entite = new RoleImplication();
        entite.setLibelle(request.getLibelle());
        entite.setActif(request.isActif());
        return entite;
    }

    public void mettreAJour(RoleImplication entite, RoleImplicationRequest request) {
        entite.setLibelle(request.getLibelle());
        entite.setActif(request.isActif());
    }

    public RoleImplicationResponse toResponse(RoleImplication entite) {
        return RoleImplicationResponse.builder()
                .id(entite.getId())
                .libelle(entite.getLibelle())
                .actif(entite.isActif())
                .build();
    }
}
