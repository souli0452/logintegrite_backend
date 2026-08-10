package bf.gov.ascelc.logintegrite_backend.dossier.repository;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.FaitReproche;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.FaitRejeteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FaitReprocheRepository extends JpaRepository<FaitReproche, UUID> {

    List<FaitReproche> findByDossierId(UUID dossierId);
    List<FaitReproche> findByDossierIdIn(List<UUID> dossierIds);

    @Query("SELECT f.typeInfraction.categorieInfraction.libelle, COUNT(f) FROM FaitReproche f GROUP BY f.typeInfraction.categorieInfraction.libelle")
    List<Object[]> compterParCategorieInfraction();

    @Query("SELECT f.zoneGeographique.libelle, COUNT(f) FROM FaitReproche f WHERE f.zoneGeographique IS NOT NULL GROUP BY f.zoneGeographique.libelle")
    List<Object[]> compterParZone();

    Page<FaitReproche> findByStatutValidation(StatutValidation statutValidation, Pageable pageable);
    List<FaitReproche> findByStatutValidation(StatutValidation statut);
}
