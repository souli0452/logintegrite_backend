package bf.gov.ascelc.logintegrite_backend.referentiel.mapper;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypeInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypeInfractionResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypeInfraction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TypeInfractionMapper {

    // categorieInfraction ignoree ici : le DTO n'a qu'un UUID, pas l'objet
    // complet - c'est le service qui va chercher la vraie entite via son
    // repository et la pose lui-meme (voir TypeInfractionServiceImpl).
    @Mapping(target = "categorieInfraction", ignore = true)
    TypeInfraction toEntity(TypeInfractionRequest request);

    @Mapping(target = "categorieInfractionId", source = "categorieInfraction.id")
    @Mapping(target = "categorieInfractionLibelle", source = "categorieInfraction.libelle")
    TypeInfractionResponse toResponse(TypeInfraction entity);
}
