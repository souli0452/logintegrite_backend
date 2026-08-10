package bf.gov.ascelc.logintegrite_backend.personne.mapper;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonnePhysique;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PersonnePhysiqueMapper {

    @Mapping(target = "nationaliteRef", ignore = true)
    @Mapping(target = "nationalite", source = "nationalite")
    PersonnePhysique toEntity(PersonnePhysiqueRequest request);

    @Mapping(target = "aUnePhoto", expression = "java(entity.getPhotoCheminStockage() != null)")
    @Mapping(target = "creeParNomComplet", expression =
        "java(entity.getCreePar() != null "
        + "? (entity.getCreePar().getPrenom() + \" \" + entity.getCreePar().getNom()) "
        + ": null)")
    @Mapping(target = "nationaliteId", expression = "java(entity.getNationaliteRef() != null ? entity.getNationaliteRef().getId() : null)")
    @Mapping(target = "nationaliteLibelle", expression = "java(entity.getNationaliteRef() != null ? entity.getNationaliteRef().getLibelle() : entity.getNationalite())")
    PersonnePhysiqueResponse toResponse(PersonnePhysique entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "nationaliteRef", ignore = true)
    void updateEntityFromRequest(PersonnePhysiqueRequest request, @MappingTarget PersonnePhysique entity);
}
