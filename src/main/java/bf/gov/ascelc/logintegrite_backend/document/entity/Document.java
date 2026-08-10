// document/entity/Document.java
package bf.gov.ascelc.logintegrite_backend.document.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypeDocument;
import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

// IdentifiableEntity seulement : ni date_maj ni version dans le DDL -
// immuabilite geree par trigger PostgreSQL (blocage UPDATE/DELETE), pas
// par verrouillage optimiste. updatable=false partout, en miroir du trigger.
@Entity
@Table(name = "document", schema = "documents")
@Getter
@Setter
public class Document extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private Dossier dossier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_document_id", nullable = false, updatable = false)
    private TypeDocument typeDocument;

    @Column(name = "nom_original", nullable = false, updatable = false)
    private String nomOriginal;

    @Column(name = "nom_stockage", nullable = false, updatable = false)
    private String nomStockage;

    @Column(name = "chemin_stockage", nullable = false, updatable = false)
    private String cheminStockage;

    @Column(name = "type_mime", nullable = false, updatable = false)
    private String typeMime;

    @Column(name = "taille_octets", nullable = false, updatable = false)
    private Long tailleOctets;

    @Column(name = "hash_integrite", nullable = false, updatable = false)
    private String hashIntegrite;

    @Column(name = "immuable", nullable = false, updatable = false)
    private boolean immuable = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploade_par_id", nullable = false, updatable = false)
    private Utilisateur uploadePar;

    @Column(name = "date_upload", insertable = false, updatable = false)
    private Instant dateUpload;
}
