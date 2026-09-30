// dossier/entity/Dossier.java
package bf.gov.ascelc.logintegrite_backend.dossier.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.VersionedAuditEntity;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutDossier;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.SourceSignalement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

// metadonnees_externes (JSONB) volontairement NON mappe pour l'instant :
// pas encore utilise fonctionnellement, et ddl-auto=validate n'exige pas
// que chaque colonne existante soit mappee - seulement que celles mappees
// existent. On l'ajoutera au moment d'un vrai besoin.
@Entity
@Table(name = "dossier", schema = "dossiers")
@Getter
@Setter
public class Dossier extends VersionedAuditEntity {

    /** Numero officiel (DOSS-2026-00001) attribue par la base a la creation : jamais saisi ni modifiable. */
    @Setter(AccessLevel.NONE)
    @Generated(event = EventType.INSERT)
    @Column(name = "numero_dossier", insertable = false, updatable = false, nullable = false)
    private String numeroDossier;

    @Column(name = "intitule")
    private String intitule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_signalement_id", nullable = false)
    private SourceSignalement sourceSignalement;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "statut_dossier", columnDefinition = "core.statut_dossier", nullable = false)
    private StatutDossier statutDossier = StatutDossier.OUVERT;

    @Column(name = "date_ouverture", nullable = false)
    private LocalDate dateOuverture = LocalDate.now();

    @Column(name = "date_cloture")
    private LocalDate dateCloture;

    @Column(name = "description_contexte", columnDefinition = "text")
    private String descriptionContexte;
}
