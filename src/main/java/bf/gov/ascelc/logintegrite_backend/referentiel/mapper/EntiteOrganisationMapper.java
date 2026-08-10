package bf.gov.ascelc.logintegrite_backend.referentiel.mapper;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.EntiteOrganisationRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.EntiteOrganisationResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.EntiteOrganisation;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.EntiteOrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntiteOrganisationMapper {

    private final EntiteOrganisationRepository repository;

    public EntiteOrganisation toEntity(EntiteOrganisationRequest request) {
        EntiteOrganisation entite = new EntiteOrganisation();
        appliquer(entite, request);
        return entite;
    }

    public void mettreAJour(EntiteOrganisation entite, EntiteOrganisationRequest request) {
        appliquer(entite, request);
    }

    // Applique les champs du request sur l'entite - resout le parent si fourni
    private void appliquer(EntiteOrganisation entite, EntiteOrganisationRequest request) {
        entite.setLibelle(request.getLibelle());
        entite.setNiveau(request.getNiveau());

        if (request.getParentId() != null) {
            EntiteOrganisation parent = repository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Entite parente", request.getParentId()));
            entite.setParent(parent);
        } else {
            entite.setParent(null);
        }
    }

    public EntiteOrganisationResponse toResponse(EntiteOrganisation entite) {
        EntiteOrganisation parent = entite.getParent();
        return EntiteOrganisationResponse.builder()
                .id(entite.getId())
                .libelle(entite.getLibelle())
                .niveau(entite.getNiveau())
                .parentId(parent != null ? parent.getId() : null)
                .parentLibelle(parent != null ? parent.getLibelle() : null)
                .build();
    }
}
