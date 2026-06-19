package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PersonnePhysiqueResponse extends AuditEntityDto {
    private String nom;
    private String prenoms;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String nationalite;
    private String matricule;
    private String fonction;
    private String photoUrl;
}