package bf.gov.ascelc.logintegrite_backend.verification.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Modeles de lecture de l'API de verification : uniquement ce qu'un compte de consultation a le droit de voir. */
public final class VerificationDtos {

    private VerificationDtos() { }

    /** Criteres de recherche d'UNE personne precise (voir VerificationService pour les combinaisons admises). */
    public record Criteres(String numeroPiece, String rccm, String ifu, String numeroPersonne,
                           String nom, String prenoms, LocalDate dateNaissance) { }

    public record Resultat(UUID id, String numeroPersonne, String nomAffichage, String typePersonne,
                           LocalDate dateNaissance, String nationalite, int nombreDossiers) { }

    public record Peine(String typePeine, String natureSanction, String duree, BigDecimal montantAmende,
                        LocalDate dateDecision, String description) { }

    public record Fait(UUID id, String typeInfraction, String categorie, LocalDate dateFaits, String lieu,
                       String description, BigDecimal montantPrejudice, String devise, BigDecimal montantConfirmeJustice,
                       String statutJudiciaire, LocalDate dateStatut, boolean issueFavorable, List<Peine> peines) { }

    public record Dossier(UUID id, String numeroDossier, String intitule, LocalDate dateOuverture, String statutDossier,
                          String role, String fonction, String entite, LocalDate dateDebut, LocalDate dateFin,
                          List<Fait> faits) { }

    public record Fiche(UUID id, String numeroPersonne, String nomAffichage, String typePersonne, LocalDate dateNaissance,
                        String nationalite, String profession, String formeJuridique, String sigle,
                        List<Dossier> dossiers) { }

    public record DemandeRequest(String motif) { }

    public record Demande(UUID id, Instant dateDemande, UUID personneId, String personne, String demandeur, String motif,
                          String statut, String traitePar, Instant dateTraitement, String commentaire) { }

    public record DecisionRequest(String decision, String commentaire) { }
}
