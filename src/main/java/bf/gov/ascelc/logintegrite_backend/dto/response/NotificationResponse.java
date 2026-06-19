package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NotificationResponse extends AuditEntityDto {
    private String destinataireId;
    private String type;
    private String contenu;
    private Boolean lue;
}