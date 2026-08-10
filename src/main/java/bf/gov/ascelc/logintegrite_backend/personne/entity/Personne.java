package bf.gov.ascelc.logintegrite_backend.personne.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import bf.gov.ascelc.logintegrite_backend.common.entity.VersionedAuditEntity;
import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePersonne;
import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "personne", schema = "personnes")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
public abstract class Personne extends VersionedAuditEntity {

    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type_personne", columnDefinition = "core.type_personne", nullable = false, updatable = false)
    private TypePersonne typePersonne;

    @Setter(AccessLevel.NONE)
    @Column(name = "nom_affichage", nullable = false)
    private String nomAffichage;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cree_par_id", nullable = false, updatable = false)
    private Utilisateur creePar;

    @OneToMany(mappedBy = "personne", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alias> aliases = new ArrayList<>();

    // --- Champs Photo ---
    
    @Setter
    @Column(name = "photo_nom_original")
    private String photoNomOriginal;

    @Setter
    @Column(name = "photo_nom_stockage")
    private String photoNomStockage;

    @Setter
    @Column(name = "photo_chemin_stockage")
    private String photoCheminStockage;

    @Setter
    @Column(name = "photo_type_mime")
    private String photoTypeMime;

    // --------------------

    @PrePersist
    protected void avantInsertion() {
        // Assignation manuelle : l'heritage JOINED exige que l'ID existe
        // AVANT l'INSERT (Hibernate doit l'ecrire simultanement dans
        // personne + personne_physique/morale), ce qui interdit la
        // generation cote base (@Generated) heritee d'IdentifiableEntity.
        // Le reflection.setAccessible bypass volontaire : on ne veut pas
        // ouvrir un setter public d'id sur IdentifiableEntity juste pour
        // ce cas d'exception - garderait un risque de mauvaise utilisation
        // partout ailleurs.
        if (getId() == null) {
            try {
                Field champ = IdentifiableEntity.class.getDeclaredField("id");
                champ.setAccessible(true);
                champ.set(this, UUID.randomUUID());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Impossible d'assigner l'id sur Personne", e);
            }
        }
        this.typePersonne = typeConcret();
        this.nomAffichage = calculerNomAffichage();
    }

    @PreUpdate
    protected void avantMaj() {
        this.nomAffichage = calculerNomAffichage();
    }

    protected abstract TypePersonne typeConcret();

    protected abstract String calculerNomAffichage();
}
