package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.PieceJointeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PieceJointeResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import java.util.List;
import java.util.UUID;

public interface PieceJointeService {
    PieceJointeResponse create(PieceJointeRequest request);

    // MODIFIÉ : infractionId ajouté, ficheId devient optionnel si infractionId fourni
    PieceJointeResponse enregistrerFichierPhysique(UUID ficheId, UUID infractionId, MultipartFile file);

    PieceJointeResponse update(UUID id, PieceJointeRequest request);
    PieceJointeResponse getById(UUID id);
    List<PieceJointeResponse> getAll();
    List<PieceJointeResponse> getByFicheId(UUID ficheId);

    // AJOUT
    List<PieceJointeResponse> getByInfractionId(UUID infractionId);
    // AJOUT : charge le fichier physique pour le servir en réponse HTTP
    Resource chargerFichierPhysique(UUID id);

    void delete(UUID id);
}
