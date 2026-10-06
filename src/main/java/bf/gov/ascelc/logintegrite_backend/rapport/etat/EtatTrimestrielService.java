package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService;
import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService.LigneRegistre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtatTrimestrielService {

    private final RapportService rapports;
    private final EtatTrimestrielRepository repository;

    /** Genere et archive l'etat d'un trimestre ; rien n'est archive si une etape echoue. */
    public void generer(PeriodeTrimestre periode, String generePar, LocalDate aujourdhui) {
        List<LigneRegistre> lignes = rapports.lireRegistre();
        Set<UUID> precedents = repository.idsDernierEtatAvant(periode);
        SyntheseTrimestre synthese = SyntheseTrimestre.calculer(precedents, lignes,
                repository.compterChangementsStatut(periode.debut(), periode.fin()),
                repository.compterDossiersClos(periode.debut(), periode.fin()));
        byte[] pdf = EtatTrimestrielDocuments.pdf(periode, aujourdhui, synthese, lignes);
        byte[] excel = EtatTrimestrielDocuments.excel(periode, aujourdhui, synthese, lignes);
        repository.archiver(periode, generePar, lignes.stream().map(LigneRegistre::personneId).toList(),
                pdf, excel, sha256(pdf), sha256(excel));
        log.info("Etat trimestriel {} archive ({} personnes)", periode.libelle(), lignes.size());
    }

    /** Genere l'etat du trimestre ecoule s'il n'existe pas encore. */
    public boolean genererSiManquant(LocalDate aujourdhui) {
        PeriodeTrimestre cible = PeriodeTrimestre.de(aujourdhui).precedent();
        if (repository.existeActif(cible)) return false;
        generer(cible, "SYSTEME", aujourdhui);
        return true;
    }

    static String sha256(byte[] octets) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(octets));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
