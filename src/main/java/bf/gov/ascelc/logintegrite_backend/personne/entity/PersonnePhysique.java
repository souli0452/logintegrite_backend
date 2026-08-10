package bf.gov.ascelc.logintegrite_backend.personne.entity;

import bf.gov.ascelc.logintegrite_backend.personne.enums.Sexe;
import bf.gov.ascelc.logintegrite_backend.personne.enums.SituationMatrimoniale;
import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePersonne;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.Nationalite;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personne_physique", schema = "personnes")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class PersonnePhysique extends Personne {

    @Column(name = "nom_naissance", nullable = false)
    private String nomNaissance;

    @Column(name = "nom_usage")
    private String nomUsage;

    @Column(name = "prenoms", nullable = false)
    private String prenoms;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "sexe", columnDefinition = "core.sexe", nullable = false)
    private Sexe sexe;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance")
    private String lieuNaissance;

    @Column(name = "nationalite", length = 100)
    private String nationalite; // Rétrocompatibilité

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationalite_id")
    private Nationalite nationaliteRef; // Source de vérité à terme

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "situation_matrimoniale", columnDefinition = "core.situation_matrimoniale")
    private SituationMatrimoniale situationMatrimoniale;

    @Column(name = "nom_conjoint")
    private String nomConjoint;

    @Column(name = "profession")
    private String profession;

    @Column(name = "matricule_fonction_publique")
    private String matriculeFonctionPublique;

    @Column(name = "grade_categorie")
    private String gradeCategorie;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "telephone")
    private String telephone;

    @OneToMany(mappedBy = "personnePhysique", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PieceIdentite> piecesIdentite = new ArrayList<>();

    @Override
    protected TypePersonne typeConcret() {
        return TypePersonne.PHYSIQUE;
    }

    @Override
    protected String calculerNomAffichage() {
        return prenoms + " " + (nomUsage != null && !nomUsage.isBlank() ? nomUsage : nomNaissance);
    }
}
