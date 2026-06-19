package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.TypeInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.TypeInfractionResponse;
import bf.gov.ascelc.logintegrite_backend.entity.TypeInfraction;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TypeInfractionMapper {

    TypeInfractionResponse toResponse(TypeInfraction entity);

    TypeInfraction toEntity(TypeInfractionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(TypeInfractionRequest request, @MappingTarget TypeInfraction entity);
}