package bf.gov.ascelc.logintegrite_backend.personne.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePieceIdentite;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypePieceIdentiteRef;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "piece_identite", schema = "personnes")
@Getter
@Setter
public class PieceIdentite extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personne_physique_id", nullable = false, updatable = false)
    private PersonnePhysique personnePhysique;

    // Ancienne colonne enum — CONSERVÉE pour rétrocompatibilité
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type_piece", columnDefinition = "core.type_piece_identite")
    private TypePieceIdentite typePiece;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_piece_id")
    private TypePieceIdentiteRef typePieceRef;

    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "date_delivrance")
    private LocalDate dateDelivrance;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;
}
