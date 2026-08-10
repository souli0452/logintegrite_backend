package bf.gov.ascelc.logintegrite_backend.parametresysteme.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "parametre_systeme", schema = "core")
@Getter
@Setter
public class ParametreSysteme extends IdentifiableEntity {

    @Column(name = "cle", nullable = false, unique = true)
    private String cle;

    @Column(name = "valeur")
    private String valeur;

    @Column(name = "description")
    private String description;
}
