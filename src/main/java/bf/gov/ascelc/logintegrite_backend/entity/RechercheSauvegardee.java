package bf.gov.ascelc.logintegrite_backend.entity;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "recherche_sauvegardee")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RechercheSauvegardee extends AuditEntity {

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String criteres;

}