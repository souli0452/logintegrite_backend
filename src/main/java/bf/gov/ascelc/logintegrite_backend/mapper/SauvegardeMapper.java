package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.SauvegardeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.SauvegardeResponse;
import bf.gov.ascelc.logintegrite_backend.entity.Sauvegarde;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SauvegardeMapper {

    SauvegardeResponse toResponse(Sauvegarde entity);

    Sauvegarde toEntity(SauvegardeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(SauvegardeRequest request, @MappingTarget Sauvegarde entity);
}