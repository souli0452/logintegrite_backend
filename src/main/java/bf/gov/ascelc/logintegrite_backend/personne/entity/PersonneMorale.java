package bf.gov.ascelc.logintegrite_backend.personne.entity;

import bf.gov.ascelc.logintegrite_backend.personne.enums.StatutPersonneMorale;
import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePersonne;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "personne_morale", schema = "personnes")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class PersonneMorale extends Personne {

    @Column(name = "denomination_sociale", nullable = false)
    private String denominationSociale;

    @Column(name = "sigle")
    private String sigle;

    @Column(name = "forme_juridique", nullable = false)
    private String formeJuridique;

    @Column(name = "rccm")
    private String rccm;

    @Column(name = "ifu")
    private String ifu;

    @Column(name = "capital_social")
    private BigDecimal capitalSocial;

    @Column(name = "secteur_activite", nullable = false)
    private String secteurActivite;

    @Column(name = "siege_social", nullable = false)
    private String siegeSocial;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "date_creation_entreprise")
    private LocalDate dateCreationEntreprise;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "statut", columnDefinition = "core.statut_personne_morale", nullable = false)
    private StatutPersonneMorale statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representant_legal_id")
    private PersonnePhysique representantLegal;

    @Override
    protected TypePersonne typeConcret() {
        return TypePersonne.MORALE;
    }

    @Override
    protected String calculerNomAffichage() {
        return denominationSociale;
    }
}
