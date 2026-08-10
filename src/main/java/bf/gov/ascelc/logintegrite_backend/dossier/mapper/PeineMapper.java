// dossier/mapper/PeineMapper.java
package bf.gov.ascelc.logintegrite_backend.dossier.mapper;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.PeineRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.PeineResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Peine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PeineMapper {

    @Mapping(target = "implicationFait", ignore = true)
    Peine toEntity(PeineRequest request);

    @Mapping(target = "implicationFaitId", source = "implicationFait.id")
    PeineResponse toResponse(Peine entity);
}
