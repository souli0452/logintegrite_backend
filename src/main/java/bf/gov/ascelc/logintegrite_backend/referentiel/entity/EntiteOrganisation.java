// referentiel/entity/EntiteOrganisation.java
package bf.gov.ascelc.logintegrite_backend.referentiel.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import bf.gov.ascelc.logintegrite_backend.referentiel.enums.NiveauEntite;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "entite_organisation", schema = "referentiels")
@Getter
@Setter
public class EntiteOrganisation extends IdentifiableEntity {

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "niveau", columnDefinition = "core.niveau_entite", nullable = false)
    private NiveauEntite niveau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private EntiteOrganisation parent;
}
