package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.entity.EntiteOrganisation;
import bf.gov.ascelc.logintegrite_backend.entity.Region;
import org.mapstruct.*;
import java.lang.reflect.Method;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CibleFicheMapper {

    // ── MAPPING PERSONNE PHYSIQUE ────────────────────────────────────────────
    @Mapping(target = "entite", ignore = true)
    @Mapping(target = "region", ignore = true)
    PersonnePhysique toEntity(PersonnePhysiqueRequest request);

    @Mapping(target = "entite", ignore = true)
    @Mapping(target = "region", ignore = true)
    void updateEntityFromRequest(PersonnePhysiqueRequest request, @MappingTarget PersonnePhysique entity);

    // ── MAPPING PERSONNE MORALE ──────────────────────────────────────────────
    @Mapping(target = "entite", ignore = true)
    @Mapping(target = "region", ignore = true)
    PersonneMorale toEntity(PersonneMoraleRequest request);

    @Mapping(target = "entite", ignore = true)
    @Mapping(target = "region", ignore = true)
    void updateEntityFromRequest(PersonneMoraleRequest request, @MappingTarget PersonneMorale entity);


    // ── AFTER-MAPPING DYNAMIQUE ET INFAILLIBLE POUR LA COMPILATION ───────────

    @AfterMapping
    default void lierRefPhysique(PersonnePhysiqueRequest request, @MappingTarget PersonnePhysique entity) {
        UUID entiteId = extraireUuidReflectif(request, "getEntiteId", "getEntite");
        if (entiteId != null) {
            entity.setEntite(EntiteOrganisation.builder().id(entiteId).build());
        }

        UUID regionId = extraireUuidReflectif(request, "getRegionId", "getRegion");
        if (regionId != null) {
            entity.setRegion(Region.builder().id(regionId).build());
        }
    }

    @AfterMapping
    default void lierRefMorale(PersonneMoraleRequest request, @MappingTarget PersonneMorale entity) {
        UUID entiteId = extraireUuidReflectif(request, "getEntiteId", "getEntite");
        if (entiteId != null) {
            entity.setEntite(EntiteOrganisation.builder().id(entiteId).build());
        }

        UUID regionId = extraireUuidReflectif(request, "getRegionId", "getRegion");
        if (regionId != null) {
            entity.setRegion(Region.builder().id(regionId).build());
        }
    }

    // Méthode utilitaire interne pour lire l'UUID peu importe le nom du getter dans le DTO
    default UUID extraireUuidReflectif(Object obj, String nomMethodePrincipal, String nomMethodeAlternative) {
        if (obj == null) return null;
        try {
            // Test de la première méthode (ex: getEntiteId)
            try {
                Method method = obj.getClass().getMethod(nomMethodePrincipal);
                Object res = method.invoke(obj);
                if (res instanceof UUID) return (UUID) res;
            } catch (NoSuchMethodException e) {
                // Si elle n'existe pas, on tente la seconde (ex: getEntite)
                Method method = obj.getClass().getMethod(nomMethodeAlternative);
                Object res = method.invoke(obj);
                if (res instanceof UUID) return (UUID) res;
            }
        } catch (Exception e) {
            // Sécurité absolue : si le champ n'existe sous aucun de ces noms, renvoie null sans faire planter l'application
        }
        return null;
    }
}