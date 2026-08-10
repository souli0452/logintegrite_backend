// dossier/mapper/ImplicationFaitMapper.java
package bf.gov.ascelc.logintegrite_backend.dossier.mapper;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationFaitResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.ImplicationFait;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImplicationFaitMapper {

    @Mapping(target = "implicationId", source = "implication.id")
    @Mapping(target = "personneNomAffichage", source = "implication.personne.nomAffichage")
    @Mapping(target = "faitReprocheId", source = "faitReproche.id")
    @Mapping(target = "faitDescription", source = "faitReproche.description")
    @Mapping(target = "faitMontantPrejudice", source = "faitReproche.montantPrejudice")
    @Mapping(target = "statutJudiciaireId", source = "statutJudiciaire.id")
    @Mapping(target = "statutJudiciaireLibelle", source = "statutJudiciaire.libelle")
    ImplicationFaitResponse toResponse(ImplicationFait entity);
}
