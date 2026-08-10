package bf.gov.ascelc.logintegrite_backend.personne.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "alias", schema = "personnes")
@Getter
@Setter
public class Alias extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personne_id", nullable = false, updatable = false)
    private Personne personne;

    @Column(name = "nom_alias", nullable = false)
    private String nomAlias;

    @Column(name = "commentaire")
    private String commentaire;
}
