package bf.gov.ascelc.logintegrite_backend.referentiel.mapper;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.NationaliteRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.NationaliteResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.Nationalite;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NationaliteMapper {

    NationaliteResponse toResponse(Nationalite entity);

    List<NationaliteResponse> toResponseList(List<Nationalite> entities);

    Nationalite toEntity(NationaliteRequest request);

    void updateEntity(NationaliteRequest request, @MappingTarget Nationalite entity);
}
