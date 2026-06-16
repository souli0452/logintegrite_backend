package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.entity.EntiteOrganisation;
import bf.gov.ascelc.logintegrite_backend.entity.Region;
import org.mapstruct.*;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CibleFicheMapper {

    // ── MAPPING PERSONNE PHYSIQUE ────────────────────────────────────────────
    @Mapping(target = "entite", source = "entiteId", qualifiedByName = "idToEntite")
    @Mapping(target = "region", source = "regionId", qualifiedByName = "idToRegion")
    PersonnePhysique toEntity(PersonnePhysiqueRequest request);

    @Mapping(target = "entite", source = "entiteId", qualifiedByName = "idToEntite")
    @Mapping(target = "region", source = "regionId", qualifiedByName = "idToRegion")
    void updateEntityFromRequest(PersonnePhysiqueRequest request, @MappingTarget PersonnePhysique entity);

    // ── MAPPING PERSONNE MORALE ──────────────────────────────────────────────
    @Mapping(target = "entite", source = "entiteId", qualifiedByName = "idToEntite")
    @Mapping(target = "region", source = "regionId", qualifiedByName = "idToRegion")
    PersonneMorale toEntity(PersonneMoraleRequest request);

    @Mapping(target = "entite", source = "entiteId", qualifiedByName = "idToEntite")
    @Mapping(target = "region", source = "regionId", qualifiedByName = "idToRegion")
    void updateEntityFromRequest(PersonneMoraleRequest request, @MappingTarget PersonneMorale entity);

    // ── CONVERTISSEURS DE RÉFÉRENTIELS ────────────────────────────────────────
    @Named("idToEntite")
    default EntiteOrganisation idToEntite(UUID entiteId) {
        if (entiteId == null) return null;
        return EntiteOrganisation.builder().id(entiteId).build();
    }

    @Named("idToRegion")
    default Region idToRegion(UUID regionId) {
        if (regionId == null) return null;
        return Region.builder().id(regionId).build();
    }
}