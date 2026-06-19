package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PersonneMoraleMapper {

    PersonneMoraleResponse toResponse(PersonneMorale entity);

    PersonneMorale toEntity(PersonneMoraleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PersonneMoraleRequest request, @MappingTarget PersonneMorale entity);
}