package bf.gov.ascelc.logintegrite_backend.referentiel.service;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.ZoneGeographiqueRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.ZoneGeographiqueResponse;

import java.util.List;
import java.util.UUID;

public interface ZoneGeographiqueService {
    List<ZoneGeographiqueResponse> lister();
    ZoneGeographiqueResponse creer(ZoneGeographiqueRequest request);
    ZoneGeographiqueResponse modifier(UUID id, ZoneGeographiqueRequest request);
    void supprimer(UUID id);
}
