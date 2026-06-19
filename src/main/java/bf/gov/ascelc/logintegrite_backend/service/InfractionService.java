package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.InfractionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.InfractionResponse;
import java.util.List;
import java.util.UUID;

public interface InfractionService {
    InfractionResponse create(InfractionRequest request);
    InfractionResponse update(UUID id, InfractionRequest request);
    InfractionResponse getById(UUID id);
    List<InfractionResponse> getAll();
    List<InfractionResponse> getByFicheId(UUID ficheId); // Pratique pour charger l'IHM Single-Page d'une fiche
    void delete(UUID id);
}