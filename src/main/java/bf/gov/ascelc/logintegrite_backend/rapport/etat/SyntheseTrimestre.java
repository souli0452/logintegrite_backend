package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService.LigneRegistre;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Ce qui a bouge depuis l'etat precedent. */
public record SyntheseTrimestre(boolean premierEtat, List<String> nouveaux, List<String> sortis,
                                long changementsStatut, long dossiersClos) {

    /** @param precedents identifiants du registre a l'etat precedent, ou null s'il n'y en a pas (premier etat). */
    public static SyntheseTrimestre calculer(Set<UUID> precedents, List<LigneRegistre> actuel,
                                             long changementsStatut, long dossiersClos) {
        if (precedents == null) {
            return new SyntheseTrimestre(true, List.of(), List.of(), changementsStatut, dossiersClos);
        }
        Set<UUID> actuels = actuel.stream().map(LigneRegistre::personneId).collect(Collectors.toSet());
        List<String> nouveaux = actuel.stream()
                .filter(l -> !precedents.contains(l.personneId()))
                .map(LigneRegistre::nom).toList();
        List<String> sortis = precedents.stream()
                .filter(id -> !actuels.contains(id))
                .map(UUID::toString).toList();
        return new SyntheseTrimestre(false, nouveaux, sortis, changementsStatut, dossiersClos);
    }
}
