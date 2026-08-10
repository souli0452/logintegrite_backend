package bf.gov.ascelc.logintegrite_backend.parametresysteme.mapper;

import bf.gov.ascelc.logintegrite_backend.parametresysteme.dto.request.ParametreSystemeRequest;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.dto.response.ParametreSystemeResponse;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.entity.ParametreSysteme;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ParametreSystemeMapper {
    ParametreSysteme toEntity(ParametreSystemeRequest request);
    ParametreSystemeResponse toResponse(ParametreSysteme entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ParametreSystemeRequest request, @MappingTarget ParametreSysteme entity);
}
