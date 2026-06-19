package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.PieceJointeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PieceJointeResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface PieceJointeService {
    PieceJointeResponse create(PieceJointeRequest request);
    PieceJointeResponse enregistrerFichierPhysique(UUID ficheId, MultipartFile file);
    PieceJointeResponse update(UUID id, PieceJointeRequest request);
    PieceJointeResponse getById(UUID id);
    List<PieceJointeResponse> getAll();
    List<PieceJointeResponse> getByFicheId(UUID ficheId);
    void delete(UUID id);
}