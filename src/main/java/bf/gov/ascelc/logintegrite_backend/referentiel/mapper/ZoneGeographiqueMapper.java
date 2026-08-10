package bf.gov.ascelc.logintegrite_backend.referentiel.mapper;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.ZoneGeographiqueRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.ZoneGeographiqueResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.ZoneGeographique;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.ZoneGeographiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ZoneGeographiqueMapper {

    private final ZoneGeographiqueRepository repository;

    public ZoneGeographique toEntity(ZoneGeographiqueRequest request) {
        ZoneGeographique entite = new ZoneGeographique();
        appliquer(entite, request);
        return entite;
    }

    public void mettreAJour(ZoneGeographique entite, ZoneGeographiqueRequest request) {
        appliquer(entite, request);
    }

    // Applique les champs du request sur l'entite - resout le parent si fourni
    private void appliquer(ZoneGeographique entite, ZoneGeographiqueRequest request) {
        entite.setLibelle(request.getLibelle());
        entite.setNiveau(request.getNiveau());
        entite.setCode(request.getCode());

        if (request.getParentId() != null) {
            ZoneGeographique parent = repository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone parente", request.getParentId()));
            entite.setParent(parent);
        } else {
            entite.setParent(null);
        }
    }

    public ZoneGeographiqueResponse toResponse(ZoneGeographique entite) {
        ZoneGeographique parent = entite.getParent();
        return ZoneGeographiqueResponse.builder()
                .id(entite.getId())
                .libelle(entite.getLibelle())
                .niveau(entite.getNiveau())
                .code(entite.getCode())
                .parentId(parent != null ? parent.getId() : null)
                .parentLibelle(parent != null ? parent.getLibelle() : null)
                .build();
    }
}
