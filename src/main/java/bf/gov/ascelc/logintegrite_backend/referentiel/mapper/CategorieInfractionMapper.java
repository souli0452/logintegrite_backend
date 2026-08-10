// referentiel/mapper/CategorieInfractionMapper.java (complet)
package bf.gov.ascelc.logintegrite_backend.referentiel.mapper;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.CategorieInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.CategorieInfractionResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.CategorieInfraction;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CategorieInfractionMapper {

    CategorieInfraction toEntity(CategorieInfractionRequest request);

    CategorieInfractionResponse toResponse(CategorieInfraction entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CategorieInfractionRequest request, @MappingTarget CategorieInfraction entity);
}
