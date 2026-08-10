package bf.gov.ascelc.logintegrite_backend.parametresysteme.service;

import bf.gov.ascelc.logintegrite_backend.parametresysteme.dto.request.ParametreSystemeRequest;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.dto.response.ParametreSystemeResponse;

import java.util.List;
import java.util.UUID;

public interface ParametreSystemeService {
    List<ParametreSystemeResponse> lister();
    ParametreSystemeResponse obtenir(UUID id);
    ParametreSystemeResponse creer(ParametreSystemeRequest request);
    ParametreSystemeResponse modifier(UUID id, ParametreSystemeRequest request);
    void supprimer(UUID id);
}
