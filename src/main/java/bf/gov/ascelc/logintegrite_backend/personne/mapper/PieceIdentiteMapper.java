package bf.gov.ascelc.logintegrite_backend.personne.mapper;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PieceIdentiteRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PieceIdentiteResponse;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PieceIdentite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PieceIdentiteMapper {

    // ─── Response (lecture) : privilégier le référentiel s'il existe ────────
    @Mapping(target = "typePieceId",
             expression = "java(entity.getTypePieceRef() != null ? entity.getTypePieceRef().getId() : null)")
    @Mapping(target = "typePieceCode",
             expression = "java(entity.getTypePieceRef() != null "
                        + "? entity.getTypePieceRef().getCode() "
                        + ": (entity.getTypePiece() != null ? entity.getTypePiece().name() : null))")
    @Mapping(target = "typePieceLibelle",
             expression = "java(entity.getTypePieceRef() != null "
                        + "? entity.getTypePieceRef().getLibelle() "
                        + ": (entity.getTypePiece() != null ? entity.getTypePiece().name() : null))")
    PieceIdentiteResponse toResponse(PieceIdentite entity);

    // ─── Entity (création) : la relation typePieceRef est résolue à la main ─
    @Mapping(target = "typePieceRef", ignore = true)
    PieceIdentite toEntity(PieceIdentiteRequest request);
}
