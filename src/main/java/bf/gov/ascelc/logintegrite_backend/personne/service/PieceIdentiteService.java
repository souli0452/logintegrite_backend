package bf.gov.ascelc.logintegrite_backend.personne.service;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PieceIdentiteRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PieceIdentiteResponse;

import java.util.List;
import java.util.UUID;

public interface PieceIdentiteService {
    List<PieceIdentiteResponse> listerParPersonnePhysique(UUID personnePhysiqueId);
    PieceIdentiteResponse creer(UUID personnePhysiqueId, PieceIdentiteRequest request);
    PieceIdentiteResponse modifier(UUID pieceId, PieceIdentiteRequest request);
    void supprimer(UUID pieceId);
}
