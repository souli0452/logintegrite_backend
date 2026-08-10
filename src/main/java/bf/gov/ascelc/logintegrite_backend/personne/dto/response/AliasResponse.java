package bf.gov.ascelc.logintegrite_backend.personne.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AliasResponse {
    private UUID id;
    private String nomAlias;
    private String commentaire;
}
