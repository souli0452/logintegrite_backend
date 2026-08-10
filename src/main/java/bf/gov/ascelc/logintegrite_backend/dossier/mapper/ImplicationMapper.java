// dossier/mapper/ImplicationMapper.java
package bf.gov.ascelc.logintegrite_backend.dossier.mapper;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.ImplicationRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImplicationMapper {

    @Mapping(target = "personne", ignore = true)
    @Mapping(target = "dossier", ignore = true)
    @Mapping(target = "roleImplication", ignore = true)
    @Mapping(target = "entiteOrganisation", ignore = true)
    @Mapping(target = "statutJudiciaire", ignore = true)
    // autoriteCompetente et referenceAffaire : MapStruct les mappe automatiquement
    // (meme nom cote request et cote entite, pas de relation a resoudre)
    Implication toEntity(ImplicationRequest request);

    @Mapping(target = "dossierId", source = "dossier.id")
    @Mapping(target = "personneId", source = "personne.id")
    @Mapping(target = "personneNomAffichage", source = "personne.nomAffichage")
    @Mapping(target = "roleImplicationId", source = "roleImplication.id")
    @Mapping(target = "roleImplicationLibelle", source = "roleImplication.libelle")
    @Mapping(target = "entiteOrganisationId", source = "entiteOrganisation.id")
    @Mapping(target = "entiteOrganisationLibelle", source = "entiteOrganisation.libelle")
    @Mapping(target = "statutJudiciaireId", source = "statutJudiciaire.id")
    @Mapping(target = "statutJudiciaireLibelle", source = "statutJudiciaire.libelle")
    // autoriteCompetente et referenceAffaire : auto-mappes
    ImplicationResponse toResponse(Implication entity);
}
