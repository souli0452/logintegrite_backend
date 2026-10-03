package bf.gov.ascelc.logintegrite_backend.personne.dto.response;

import java.util.UUID;

/** Resultat du controle d'un NIP : libre, ou deja porte par une personne (que l'on designe pour eviter un doublon). */
public record VerificationNipResponse(
        boolean disponible,
        UUID personneExistanteId,
        String personneExistanteNomAffichage,
        String personneExistanteType) {

    public static VerificationNipResponse libre() {
        return new VerificationNipResponse(true, null, null, null);
    }

    public static VerificationNipResponse dejaPris(UUID id, String nomAffichage, String type) {
        return new VerificationNipResponse(false, id, nomAffichage, type);
    }
}
