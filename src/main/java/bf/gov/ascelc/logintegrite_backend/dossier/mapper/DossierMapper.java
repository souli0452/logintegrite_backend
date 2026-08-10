// dossier/mapper/DossierMapper.java (complet)
package bf.gov.ascelc.logintegrite_backend.dossier.mapper;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.DossierRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.DossierResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface DossierMapper {

    @Mapping(target = "sourceSignalement", ignore = true)
    Dossier toEntity(DossierRequest request);

    @Mapping(target = "sourceSignalementId", source = "sourceSignalement.id")
    @Mapping(target = "sourceSignalementLibelle", source = "sourceSignalement.libelle")
    DossierResponse toResponse(Dossier entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "sourceSignalement", ignore = true)
    void updateEntityFromRequest(DossierRequest request, @MappingTarget Dossier entity);
}
