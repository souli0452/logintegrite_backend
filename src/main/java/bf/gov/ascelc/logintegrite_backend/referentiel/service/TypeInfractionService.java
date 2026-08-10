package bf.gov.ascelc.logintegrite_backend.referentiel.service;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypeInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypeInfractionResponse;

import java.util.List;
import java.util.UUID;

public interface TypeInfractionService {
    List<TypeInfractionResponse> lister();
    TypeInfractionResponse obtenir(UUID id);
    TypeInfractionResponse creer(TypeInfractionRequest request);
    TypeInfractionResponse modifier(UUID id, TypeInfractionRequest request);
    void supprimer(UUID id);
}
