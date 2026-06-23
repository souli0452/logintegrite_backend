package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiquePublicResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PersonnePhysiqueMapper {

    PersonnePhysiqueResponse toResponse(PersonnePhysique entity);

    PersonnePhysiquePublicResponse toPublicResponse(PersonnePhysique entity);

    List<PersonnePhysiquePublicResponse> toPublicResponseList(List<PersonnePhysique> entities);

    PersonnePhysique toEntity(PersonnePhysiqueRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PersonnePhysiqueRequest request, @MappingTarget PersonnePhysique entity);
}