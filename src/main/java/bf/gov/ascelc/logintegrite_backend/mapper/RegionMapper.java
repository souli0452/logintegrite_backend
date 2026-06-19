package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.RegionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.RegionResponse;
import bf.gov.ascelc.logintegrite_backend.entity.Region;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RegionMapper {

    RegionResponse toResponse(Region entity);

    Region toEntity(RegionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(RegionRequest request, @MappingTarget Region entity);
}