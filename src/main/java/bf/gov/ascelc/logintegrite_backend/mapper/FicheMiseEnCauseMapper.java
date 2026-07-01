package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheDetailResponse;
import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import org.mapstruct.*;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {InfractionMapper.class, PieceJointeMapper.class, HistoriqueStatutMapper.class}
)
public interface FicheMiseEnCauseMapper {

    // 1. MAPPINGS POUR LE TABLEAU DE BORD (LISTE FLUIDE)
    @SubclassMapping(source = PersonnePhysique.class, target = FicheMiseEnCauseResponse.class)
    @SubclassMapping(source = PersonneMorale.class, target = FicheMiseEnCauseResponse.class)
    FicheMiseEnCauseResponse toResponse(FicheMiseEnCause fiche);

    @Mapping(target = "typeFiche", constant = "PP")
    @Mapping(target = "dateModification", source = "updatedAt")
    @Mapping(target = "entiteNom", source = "entite.nom")
    @Mapping(target = "regionNom", source = "region.nom")
    @Mapping(target = "identifiantUnique", source = "matricule")
    @Mapping(target = "cibleNom", expression = "java(pp.getNom() + \" \" + (pp.getPrenoms() != null ? pp.getPrenoms() : \"\"))")
    FicheMiseEnCauseResponse ppToResponse(PersonnePhysique pp);

    @Mapping(target = "typeFiche", constant = "PM")
    @Mapping(target = "dateModification", source = "updatedAt")
    @Mapping(target = "entiteNom", source = "entite.nom")
    @Mapping(target = "regionNom", source = "region.nom")
    @Mapping(target = "identifiantUnique", source = "ifu")
    @Mapping(target = "cibleNom", source = "raisonSociale")
    FicheMiseEnCauseResponse pmToResponse(PersonneMorale pm);


    // 2. MAPPINGS POUR LA VUE DÉTAILLÉE (ACCORDÉONS ANGULAR)

    @SubclassMapping(source = PersonnePhysique.class, target = FicheDetailResponse.class)
    @SubclassMapping(source = PersonneMorale.class, target = FicheDetailResponse.class)
    FicheDetailResponse toDetailResponse(FicheMiseEnCause fiche);

    // ── MAPPING DÉTAILLÉ : PERSONNE PHYSIQUE ─────────────────────────────────

    @Mapping(target = "entiteNom", source = "entite.nom")
    @Mapping(target = "regionNom", source = "region.nom")
    @Mapping(target = "identifiantUnique", source = "matricule")
    FicheDetailResponse ppToDetailResponse(PersonnePhysique pp);

    // ── MAPPING DÉTAILLÉ : PERSONNE MORALE ───────────────────────────────────
    @Mapping(target = "typeFiche", constant = "PM")
    @Mapping(target = "entiteNom", source = "entite.nom")
    @Mapping(target = "regionNom", source = "region.nom")
    @Mapping(target = "identifiantUnique", source = "ifu")
    FicheDetailResponse pmToDetailResponse(PersonneMorale pm);


}