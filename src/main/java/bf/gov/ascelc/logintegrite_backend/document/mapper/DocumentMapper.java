// document/mapper/DocumentMapper.java
package bf.gov.ascelc.logintegrite_backend.document.mapper;

import bf.gov.ascelc.logintegrite_backend.document.dto.response.DocumentResponse;
import bf.gov.ascelc.logintegrite_backend.document.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "dossierId", source = "dossier.id")
    @Mapping(target = "typeDocumentId", source = "typeDocument.id")
    @Mapping(target = "typeDocumentLibelle", source = "typeDocument.libelle")
    @Mapping(target = "uploadeParId", source = "uploadePar.id")
    @Mapping(target = "uploadeParNomComplet", expression =
        "java(entity.getUploadePar() != null ? entity.getUploadePar().getPrenom() + \" \" + entity.getUploadePar().getNom() : null)")
    DocumentResponse toResponse(Document entity);
}
