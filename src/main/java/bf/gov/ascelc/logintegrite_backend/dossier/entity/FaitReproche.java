// dossier/entity/FaitReproche.java
package bf.gov.ascelc.logintegrite_backend.dossier.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.VersionedAuditEntity;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypeInfraction;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.ZoneGeographique;
import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "fait_reproche", schema = "dossiers")
@Getter
@Setter
public class FaitReproche extends VersionedAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private Dossier dossier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_infraction_id", nullable = false)
    private TypeInfraction typeInfraction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_geographique_id")
    private ZoneGeographique zoneGeographique;

    @Column(name = "date_faits", nullable = false)
    private LocalDate dateFaits;

    @Column(name = "lieu_precis")
    private String lieuPrecis;

    @Column(name = "description",columnDefinition = "text" , nullable = false)
    private String description;

    @Column(name = "montant_prejudice", nullable = false)
    private BigDecimal montantPrejudice;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise = "XOF";

    @Column(name = "montant_confirme_justice")
    private BigDecimal montantConfirmeJustice;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "statut_validation", columnDefinition = "core.statut_validation", nullable = false)
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE;

    @Column(name = "motif_rejet")
    private String motifRejet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par_id")
    private Utilisateur validePar;

    @Column(name = "date_validation")
    private Instant dateValidation;
}
