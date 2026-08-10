package bf.gov.ascelc.logintegrite_backend.referentiel.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categorie_infraction", schema = "referentiels")
@Getter
@Setter
public class CategorieInfraction extends IdentifiableEntity {

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    @Column(name = "description")
    private String description;
}
