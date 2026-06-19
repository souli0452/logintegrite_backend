package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.HistoriqueStatutRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.HistoriqueStatutResponse;
import bf.gov.ascelc.logintegrite_backend.entity.HistoriqueStatut;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HistoriqueStatutMapper {

    @Mapping(source = "fiche.id", target = "ficheId")
    HistoriqueStatutResponse toResponse(HistoriqueStatut entity);

    @Mapping(target = "fiche", ignore = true) // Assigné manuellement dans le service
    HistoriqueStatut toEntity(HistoriqueStatutRequest request);

    @Mapping(target = "fiche", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(HistoriqueStatutRequest request, @MappingTarget HistoriqueStatut entity);
}