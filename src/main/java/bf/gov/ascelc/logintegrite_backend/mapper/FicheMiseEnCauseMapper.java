package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING, // Permet d'injecter le mapper avec @Autowired/@RequiredArgsConstructor
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FicheMiseEnCauseMapper {

    // ── CONFIGURATION DE L'HÉRITAGE D'ENTITÉS ─────────────────────────────────
    // MapStruct va automatiquement rediriger l'appel vers la bonne méthode de mapping selon le type réel
    @SubclassMapping(source = PersonnePhysique.class, target = FicheMiseEnCauseResponse.class)
    @SubclassMapping(source = PersonneMorale.class, target = FicheMiseEnCauseResponse.class)
    FicheMiseEnCauseResponse toResponse(FicheMiseEnCause fiche);

    // ── MAPPING DÉTAILLÉ : PERSONNE PHYSIQUE ─────────────────────────────────
    @Mapping(target = "typeFiche", constant = "PP")
    @Mapping(target = "dateModification", source = "updatedAt")
    @Mapping(target = "entiteNom", source = "entite.nom")
    @Mapping(target = "regionNom", source = "region.nom")
    @Mapping(target = "identifiantUnique", source = "matricule")
    @Mapping(target = "cibleNom", expression = "java(pp.getNom() + \" \" + (pp.getPrenoms() != null ? pp.getPrenoms() : \"\"))")
    FicheMiseEnCauseResponse ppToResponse(PersonnePhysique pp);

    // ── MAPPING DÉTAILLÉ : PERSONNE MORALE ───────────────────────────────────
    @Mapping(target = "typeFiche", constant = "PM")
    @Mapping(target = "dateModification", source = "updatedAt")
    @Mapping(target = "entiteNom", source = "entite.nom")
    @Mapping(target = "regionNom", source = "region.nom")
    @Mapping(target = "identifiantUnique", source = "ifu")
    @Mapping(target = "cibleNom", source = "raisonSociale")
    FicheMiseEnCauseResponse pmToResponse(PersonneMorale pm);
}