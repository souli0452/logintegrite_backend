package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.InfractionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.InfractionResponse;
import bf.gov.ascelc.logintegrite_backend.entity.Infraction;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface InfractionMapper {

    @Mapping(source = "fiche.id", target = "ficheId")
    @Mapping(source = "typeInfraction.id", target = "typeInfractionId")
    @Mapping(source = "typeInfraction.libelle", target = "typeInfractionLibelle")
    @Mapping(source = "typeInfraction.code", target = "typeInfractionCode")
    InfractionResponse toResponse(Infraction entity);

    @Mapping(target = "fiche", ignore = true) // Géré manuellement dans le service pour des raisons de sécurité JPA
    @Mapping(target = "typeInfraction", ignore = true)
    Infraction toEntity(InfractionRequest request);

    @Mapping(target = "fiche", ignore = true)
    @Mapping(target = "typeInfraction", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(InfractionRequest request, @MappingTarget Infraction entity);
}