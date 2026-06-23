package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoralePublicResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PersonneMoraleMapper {

    PersonneMoraleResponse toResponse(PersonneMorale entity);

    PersonneMoralePublicResponse toPublicResponse(PersonneMorale entity);

    List<PersonneMoralePublicResponse> toPublicResponseList(List<PersonneMorale> entities);

    PersonneMorale toEntity(PersonneMoraleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PersonneMoraleRequest request, @MappingTarget PersonneMorale entity);
}