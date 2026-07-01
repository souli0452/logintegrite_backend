package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.HistoriqueStatutRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.HistoriqueStatutResponse;
import bf.gov.ascelc.logintegrite_backend.entity.HistoriqueStatut;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface HistoriqueStatutMapper {

    @Mapping(source = "fiche.id", target = "ficheId")
    @Mapping(source = "infraction.id", target = "infractionId")
    HistoriqueStatutResponse toResponse(HistoriqueStatut entity);

    @Mapping(target = "fiche", ignore = true)
    @Mapping(target = "infraction", ignore = true)
    HistoriqueStatut toEntity(HistoriqueStatutRequest request);

    @Mapping(target = "fiche", ignore = true)
    @Mapping(target = "infraction", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(HistoriqueStatutRequest request, @MappingTarget HistoriqueStatut entity);
}