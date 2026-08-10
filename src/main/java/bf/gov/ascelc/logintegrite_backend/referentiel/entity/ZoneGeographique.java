// referentiel/entity/ZoneGeographique.java
package bf.gov.ascelc.logintegrite_backend.referentiel.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import bf.gov.ascelc.logintegrite_backend.referentiel.enums.NiveauZone;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "zone_geographique", schema = "referentiels")
@Getter
@Setter
public class ZoneGeographique extends IdentifiableEntity {

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "niveau", columnDefinition = "core.niveau_zone", nullable = false)
    private NiveauZone niveau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ZoneGeographique parent;

    @Column(name = "code")
    private String code;
}
