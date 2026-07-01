package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.RechercheSauvegardeeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.RechercheSauvegardeeResponse;
import bf.gov.ascelc.logintegrite_backend.entity.RechercheSauvegardee;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RechercheSauvegardeeMapper {

    RechercheSauvegardeeResponse toResponse(RechercheSauvegardee entity);

    RechercheSauvegardee toEntity(RechercheSauvegardeeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(RechercheSauvegardeeRequest request, @MappingTarget RechercheSauvegardee entity);
}