// securite/mapper/RoleHabilitationMapper.java (complet)
package bf.gov.ascelc.logintegrite_backend.securite.mapper;

import bf.gov.ascelc.logintegrite_backend.securite.dto.request.RoleHabilitationRequest;
import bf.gov.ascelc.logintegrite_backend.securite.dto.response.RoleHabilitationResponse;
import bf.gov.ascelc.logintegrite_backend.securite.entity.RoleHabilitation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RoleHabilitationMapper {

    RoleHabilitation toEntity(RoleHabilitationRequest request);

    RoleHabilitationResponse toResponse(RoleHabilitation entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(RoleHabilitationRequest request, @MappingTarget RoleHabilitation entity);
}
