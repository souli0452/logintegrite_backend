package bf.gov.ascelc.logintegrite_backend.securite.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

// IdentifiableEntity seulement (pas VersionedAuditEntity) : la table
// utilisateur a bien date_creation + date_maj, mais PAS de colonne
// version dans le DDL - verifie, pas suppose.
@Entity
@Table(name = "utilisateur", schema = "securite")
@Getter
@Setter
public class Utilisateur extends IdentifiableEntity {

    @Column(name = "keycloak_id", nullable = false, unique = true, updatable = false)
    private String keycloakId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "date_creation", insertable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "date_maj", insertable = false)
    private Instant dateMaj;
}