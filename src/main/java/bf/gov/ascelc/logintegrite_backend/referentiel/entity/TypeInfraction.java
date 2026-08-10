package bf.gov.ascelc.logintegrite_backend.referentiel.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "type_infraction", schema = "referentiels")
@Getter
@Setter
public class TypeInfraction extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_infraction_id", nullable = false)
    private CategorieInfraction categorieInfraction;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;
}
