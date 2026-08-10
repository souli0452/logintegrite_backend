package bf.gov.ascelc.logintegrite_backend.referentiel.service;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypePieceIdentiteRefRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypePieceIdentiteRefResponse;

import java.util.List;
import java.util.UUID;

public interface TypePieceIdentiteRefService {
    List<TypePieceIdentiteRefResponse> lister();
    List<TypePieceIdentiteRefResponse> listerActifs();
    TypePieceIdentiteRefResponse obtenir(UUID id);
    TypePieceIdentiteRefResponse creer(TypePieceIdentiteRefRequest request);
    TypePieceIdentiteRefResponse modifier(UUID id, TypePieceIdentiteRefRequest request);
    void supprimer(UUID id);
}
