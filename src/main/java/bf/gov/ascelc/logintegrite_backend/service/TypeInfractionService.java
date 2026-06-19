package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.TypeInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.TypeInfractionResponse;
import java.util.List;
import java.util.UUID;

public interface TypeInfractionService {
    TypeInfractionResponse create(TypeInfractionRequest request);
    TypeInfractionResponse update(UUID id, TypeInfractionRequest request);
    TypeInfractionResponse getById(UUID id);
    List<TypeInfractionResponse> getAll();
    void delete(UUID id);
}