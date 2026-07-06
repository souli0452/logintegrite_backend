package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiquePublicResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import org.mapstruct.*;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE // ── MAGIE : Supprime tous les warnings de ce fichier ──
)
public interface PersonnePhysiqueMapper {

    @Mapping(target = "typeFiche", constant = "PM")
    @Mapping(target = "regionNom", source = "region.nom")
    @Mapping(target = "entiteNom", source = "entite.nom")
    PersonnePhysiqueResponse toResponse(PersonnePhysique entity);

    PersonnePhysiquePublicResponse toPublicResponse(PersonnePhysique entity);

    List<PersonnePhysiquePublicResponse> toPublicResponseList(List<PersonnePhysique> entities);

    @Mapping(target = "region.id", source = "regionId")
    @Mapping(target = "entite.id", source = "entiteId")
    PersonnePhysique toEntity(PersonnePhysiqueRequest request);

    @Mapping(target = "region.id", source = "regionId")
    @Mapping(target = "entite.id", source = "entiteId")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PersonnePhysiqueRequest request, @MappingTarget PersonnePhysique entity);
}