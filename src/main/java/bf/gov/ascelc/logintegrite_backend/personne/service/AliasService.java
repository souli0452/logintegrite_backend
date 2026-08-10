package bf.gov.ascelc.logintegrite_backend.personne.service;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.AliasRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.AliasResponse;

import java.util.List;
import java.util.UUID;

public interface AliasService {
    List<AliasResponse> listerParPersonne(UUID personneId);
    AliasResponse creer(UUID personneId, AliasRequest request);
    void supprimer(UUID aliasId);
}
