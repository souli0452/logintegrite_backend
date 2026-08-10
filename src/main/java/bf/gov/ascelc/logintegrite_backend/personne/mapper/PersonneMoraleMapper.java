package bf.gov.ascelc.logintegrite_backend.personne.mapper;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonneMorale;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PersonneMoraleMapper {

    @Mapping(target = "representantLegal", ignore = true)
    PersonneMorale toEntity(PersonneMoraleRequest request);

    @Mapping(target = "aUnLogo", expression = "java(entity.getPhotoCheminStockage() != null)")
    @Mapping(target = "representantLegalId", source = "representantLegal.id")
    @Mapping(target = "representantLegalNomComplet", expression =
        "java(entity.getRepresentantLegal() != null ? entity.getRepresentantLegal().getNomAffichage() : null)")
    @Mapping(target = "creeParNomComplet", expression =
        "java(entity.getCreePar() != null "
        + "? (entity.getCreePar().getPrenom() + \" \" + entity.getCreePar().getNom()) "
        + ": null)")
    PersonneMoraleResponse toResponse(PersonneMorale entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "representantLegal", ignore = true)
    void updateEntityFromRequest(PersonneMoraleRequest request, @MappingTarget PersonneMorale entity);
}
