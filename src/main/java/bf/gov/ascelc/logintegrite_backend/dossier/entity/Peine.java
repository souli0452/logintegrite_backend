// dossier/entity/Peine.java
package bf.gov.ascelc.logintegrite_backend.dossier.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.NatureSanction;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.TypePeine;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

// IdentifiableEntity seulement : peine n'a que date_creation dans le DDL
@Entity
@Table(name = "peine", schema = "dossiers")
@Getter
@Setter
public class Peine extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "implication_fait_id", nullable = false, updatable = false)
    private ImplicationFait implicationFait;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type_peine", columnDefinition = "core.type_peine", nullable = false)
    private TypePeine typePeine;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "nature_sanction", columnDefinition = "core.nature_sanction")
    private NatureSanction natureSanction;

    @Column(name = "duree")
    private String duree;

    @Column(name = "montant_amende")
    private BigDecimal montantAmende;

    @Column(name = "date_decision")
    private LocalDate dateDecision;

    @Column(name = "date_execution")
    private LocalDate dateExecution;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "date_creation", insertable = false, updatable = false)
    private Instant dateCreation;
}
