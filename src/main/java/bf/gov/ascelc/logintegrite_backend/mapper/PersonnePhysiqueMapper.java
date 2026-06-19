package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PersonnePhysiqueMapper {

    PersonnePhysiqueResponse toResponse(PersonnePhysique entity);

    PersonnePhysique toEntity(PersonnePhysiqueRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PersonnePhysiqueRequest request, @MappingTarget PersonnePhysique entity);
}