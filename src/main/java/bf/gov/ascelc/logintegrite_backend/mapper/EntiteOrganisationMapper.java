package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.EntiteOrganisationRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.EntiteOrganisationResponse;
import bf.gov.ascelc.logintegrite_backend.entity.EntiteOrganisation;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EntiteOrganisationMapper {

    EntiteOrganisationResponse toResponse(EntiteOrganisation entity);

    EntiteOrganisation toEntity(EntiteOrganisationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(EntiteOrganisationRequest request, @MappingTarget EntiteOrganisation entity);
}