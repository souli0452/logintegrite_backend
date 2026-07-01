package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireReferentielRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.StatutJudiciaireReferentielResponse;
import bf.gov.ascelc.logintegrite_backend.entity.StatutJudiciaireReferentiel;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface StatutJudiciaireReferentielMapper {

    StatutJudiciaireReferentielResponse toResponse(StatutJudiciaireReferentiel entity);

    StatutJudiciaireReferentiel toEntity(StatutJudiciaireReferentielRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(StatutJudiciaireReferentielRequest request, @MappingTarget StatutJudiciaireReferentiel entity);
}