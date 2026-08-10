package bf.gov.ascelc.logintegrite_backend.referentiel.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.NationaliteRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.NationaliteResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.Nationalite;
import bf.gov.ascelc.logintegrite_backend.referentiel.mapper.NationaliteMapper;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.NationaliteRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.NationaliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Transactional
public class NationaliteServiceImpl implements NationaliteService {

    private final NationaliteRepository repository;
    private final NationaliteMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<NationaliteResponse> lister() {
        List<Nationalite> liste = repository.findAll();
        // Tri : Burkinabè en premier, puis alphabétique
        liste.sort(this::comparerNationalites);
        return mapper.toResponseList(liste);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NationaliteResponse> listerActifs() {
        List<Nationalite> liste = repository.findByActifTrueOrderByLibelleAsc();
        liste.sort(this::comparerNationalites);
        return mapper.toResponseList(liste);
    }

    /**
     * Tri personnalisé : Burkinabè en premier, puis alphabétique.
     */
    private int comparerNationalites(Nationalite a, Nationalite b) {
        if ("Burkinabè".equalsIgnoreCase(a.getLibelle())) return -1;
        if ("Burkinabè".equalsIgnoreCase(b.getLibelle())) return 1;
        return a.getLibelle().compareToIgnoreCase(b.getLibelle());
    }

    @Override
    @Transactional(readOnly = true)
    public NationaliteResponse obtenir(UUID id) {
        return mapper.toResponse(
            repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nationalité", id))
        );
    }

    @Override
    public NationaliteResponse creer(NationaliteRequest request) {
        Nationalite entite = mapper.toEntity(request);
        if (entite.isActif() == false && request.getActif() == null) {
            entite.setActif(true);
        }
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public NationaliteResponse modifier(UUID id, NationaliteRequest request) {
        Nationalite entite = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Nationalité", id));
        mapper.updateEntity(request, entite);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public void supprimer(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Nationalité", id);
        }
        repository.deleteById(id);
    }
}
