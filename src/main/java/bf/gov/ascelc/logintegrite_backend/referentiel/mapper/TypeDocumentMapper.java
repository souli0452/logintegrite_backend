package bf.gov.ascelc.logintegrite_backend.referentiel.mapper;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypeDocumentRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypeDocumentResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypeDocument;
import org.springframework.stereotype.Component;

@Component
public class TypeDocumentMapper {

    public TypeDocument toEntity(TypeDocumentRequest request) {
        TypeDocument entite = new TypeDocument();
        entite.setLibelle(request.getLibelle());
        entite.setActif(request.isActif());
        return entite;
    }

    public void mettreAJour(TypeDocument entite, TypeDocumentRequest request) {
        entite.setLibelle(request.getLibelle());
        entite.setActif(request.isActif());
    }

    public TypeDocumentResponse toResponse(TypeDocument entite) {
        return TypeDocumentResponse.builder()
                .id(entite.getId())
                .libelle(entite.getLibelle())
                .actif(entite.isActif())
                .build();
    }
}
