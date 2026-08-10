package bf.gov.ascelc.logintegrite_backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * A n'utiliser QUE pour les 4 tables qui portent vraiment ces 3 colonnes
 * ensemble : Personne, Dossier, FaitReproche, ImplicationFait.
 * Verifie contre le DDL, pas suppose : la plupart des autres tables
 * (referentiels, Alias, PieceIdentite, Peine...) n'ont que date_creation
 * seule, ou meme aucune colonne d'audit - les forcer a heriter de ces 3
 * champs ferait echouer ddl-auto=validate au demarrage (colonnes absentes).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class VersionedAuditEntity extends IdentifiableEntity {

    @Column(name = "date_creation", updatable = false, insertable = false, nullable = false)
    private Instant dateCreation;

    @Column(name = "date_maj", insertable = false, nullable = false)
    private Instant dateMaj;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
