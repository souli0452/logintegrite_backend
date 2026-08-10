// dossier/mapper/FaitReprocheMapper.java
package bf.gov.ascelc.logintegrite_backend.dossier.mapper;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.FaitReprocheRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.FaitReprocheResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.FaitReproche;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FaitReprocheMapper {

    @Mapping(target = "dossier", ignore = true)
    @Mapping(target = "typeInfraction", ignore = true)
    @Mapping(target = "zoneGeographique", ignore = true)
    FaitReproche toEntity(FaitReprocheRequest request);

    @Mapping(target = "dossierId", source = "dossier.id")
    @Mapping(target = "typeInfractionId", source = "typeInfraction.id")
    @Mapping(target = "typeInfractionLibelle", source = "typeInfraction.libelle")
    @Mapping(target = "zoneGeographiqueId", source = "zoneGeographique.id")
    @Mapping(target = "zoneGeographiqueLibelle", source = "zoneGeographique.libelle")
    @Mapping(target = "validateParId", source = "validePar.id")
    @Mapping(target = "validateParNomComplet", expression =
        "java(entity.getValidePar() != null ? entity.getValidePar().getPrenom() + \" \" + entity.getValidePar().getNom() : null)")
    FaitReprocheResponse toResponse(FaitReproche entity);
}
