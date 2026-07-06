package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoralePublicResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import org.mapstruct.*;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE // ── MAGIE : Supprime tous les warnings de ce fichier ──
)
public interface PersonneMoraleMapper {

    @Mapping(target = "typeFiche", constant = "PM")
    @Mapping(target = "regionNom", source = "region.nom")
    @Mapping(target = "entiteNom", source = "entite.nom")
    PersonneMoraleResponse toResponse(PersonneMorale entity);

    // CORRECTION : On a enlevé les @Mapping obsolètes qui bloquaient le build
    PersonneMoralePublicResponse toPublicResponse(PersonneMorale entity);

    List<PersonneMoralePublicResponse> toPublicResponseList(List<PersonneMorale> entities);

    @Mapping(source = "regionId", target = "region.id")
    @Mapping(source = "entiteId", target = "entite.id")
    PersonneMorale toEntity(PersonneMoraleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "regionId", target = "region.id")
    @Mapping(source = "entiteId", target = "entite.id")
    void updateEntityFromRequest(PersonneMoraleRequest request, @MappingTarget PersonneMorale entity);
}