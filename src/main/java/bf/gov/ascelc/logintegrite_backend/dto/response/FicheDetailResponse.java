package bf.gov.ascelc.logintegrite_backend.dto.response;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntityDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class FicheDetailResponse extends AuditEntityDto {
    private UUID id;
    private String typeFiche;
    private String statutFiche;
    private String statutJudiciaire;
    private String motifRejet;

    // Données d'ancrage
    private String identifiantUnique;
    private String entiteNom;
    private String regionNom;

    // Champs spécifiques Personne Physique (PP)
    private String nom;
    private String prenoms;
    private String matricule;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String nationalite;
    private String fonction;
    private String photoUrl;

    // Champs spécifiques Personne Morale (PM)
    private String raisonSociale;
    private String sigle;
    private String ifu;
    private String typeStructure;
    private String nomResponsable;
    private String fonctionResponsable;

    // CORRIGÉ : List<?> empêchait MapStruct d'appeler les convertisseurs dédiés
    // (toInfractionResponseList / toPieceJointeResponseList) et laissait fuiter
    // les entités JPA brutes dans le JSON
    private List<InfractionResponse> infractions;
    private List<PieceJointeResponse> piecesJointes;
    private List<HistoriqueStatutResponse> historiqueStatuts;
}