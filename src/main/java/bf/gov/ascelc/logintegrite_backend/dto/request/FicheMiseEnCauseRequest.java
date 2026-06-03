package bf.gov.ascelc.logintegrite_backend.dto.request;

import bf.gov.ascelc.logintegrite_backend.entity.Infraction.NatureInfraction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FicheMiseEnCauseRequest {

    @NotBlank(message = "Le type de cible (PHYSIQUE ou MORALE) est obligatoire")
    private String typeCible;

    // ── CHAMPS POUR PERSONNE PHYSIQUE ──────────────────────────
    // On enlève le @NotBlank global car ces champs seront vides si typeCible = MORALE
    private String nom;
    private String prenoms;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String nationalite;
    private String matricule;
    private String fonction;

    // ── CHAMPS POUR PERSONNE MORALE ────────────────────────────
    private String raisonSociale;
    private String sigle;
    private String ifu;
    private String regimeJuridique;
    private String siegeSocial;

    // ── COUPLAGE INFRASTRUCTURES & RAPPORTS (ASCE-LC) ──────────
    @NotNull(message = "L'entité ou organisation liée est obligatoire")
    private Long entiteId;

    @NotNull(message = "La région est obligatoire")
    private Long regionId;

    // ── INFRACTION PRINCIPALE ASSOCIÉE ─────────────────────────
    @NotNull(message = "La nature de l'infraction est obligatoire")
    private NatureInfraction natureInfraction;

    @NotNull(message = "La date des faits est obligatoire")
    private LocalDate dateFaits;

    private String lieuFaits;
    private String descriptionFaits;
    private Double montant;
    private String devise;
    private Long typeInfractionId;
}
