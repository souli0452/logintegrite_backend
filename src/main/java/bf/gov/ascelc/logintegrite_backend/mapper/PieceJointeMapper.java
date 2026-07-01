package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.PieceJointeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PieceJointeResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PieceJointe;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE // Ajouté ici
)
public interface PieceJointeMapper {

    @Mapping(source = "fiche.id", target = "ficheId")
    PieceJointeResponse toResponse(PieceJointe entity);

    @Mapping(target = "fiche", ignore = true)
    PieceJointe toEntity(PieceJointeRequest request);

    @Mapping(target = "fiche", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PieceJointeRequest request, @MappingTarget PieceJointe entity);
}