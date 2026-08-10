package bf.gov.ascelc.logintegrite_backend.referentiel.mapper;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypePieceIdentiteRefRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypePieceIdentiteRefResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypePieceIdentiteRef;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TypePieceIdentiteRefMapper {

    TypePieceIdentiteRefResponse toResponse(TypePieceIdentiteRef entity);

    List<TypePieceIdentiteRefResponse> toResponseList(List<TypePieceIdentiteRef> entities);

    TypePieceIdentiteRef toEntity(TypePieceIdentiteRefRequest request);

    /**
     * Update partielle : le code est immuable après création — on l'ignore ici
     * pour éviter les altérations accidentelles qui casseraient le code Java.
     */
    @Mapping(target = "code", ignore = true)
    void updateEntity(TypePieceIdentiteRefRequest request, @MappingTarget TypePieceIdentiteRef entity);
}
