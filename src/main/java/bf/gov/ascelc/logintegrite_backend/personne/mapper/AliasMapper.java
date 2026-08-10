package bf.gov.ascelc.logintegrite_backend.personne.mapper;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.AliasRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.AliasResponse;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Alias;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AliasMapper {
    AliasResponse toResponse(Alias entity);
}
