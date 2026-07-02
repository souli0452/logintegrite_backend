package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatistiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.StatistiqueResponse;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.repository.InfractionRepository;
import bf.gov.ascelc.logintegrite_backend.repository.PersonneMoraleRepository;
import bf.gov.ascelc.logintegrite_backend.repository.PersonnePhysiqueRepository;
import bf.gov.ascelc.logintegrite_backend.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatistiqueServiceImpl implements StatistiqueService {

    private final FicheMiseEnCauseRepository ficheRepo;
    private final PersonnePhysiqueRepository ppRepo;
    private final PersonneMoraleRepository pmRepo;
    private final InfractionRepository infractionRepo;

    @Override
    @Transactional(readOnly = true)
    public StatistiqueResponse calculer(StatistiqueRequest filtres) {

        LocalDateTime debut = filtres.getDateDebut() != null ? filtres.getDateDebut().atStartOfDay() : null;
        LocalDateTime fin = filtres.getDateFin() != null ? filtres.getDateFin().atTime(LocalTime.MAX) : null;
        UUID regionId = filtres.getRegionId();
        UUID entiteId = filtres.getEntiteId();
        UUID typeInfractionId = filtres.getTypeInfractionId();
        String groupBy = (filtres.getGroupBy() != null && !filtres.getGroupBy().isBlank())
                ? filtres.getGroupBy().toUpperCase()
                : "REGION";

        long total = ficheRepo.countTotalFiltre(regionId, entiteId, typeInfractionId, debut, fin);
        long actives = ficheRepo.countByStatutFiltre("ACTIVE", regionId, entiteId, typeInfractionId, debut, fin);
        long enAttente = ficheRepo.countByStatutFiltre("EN_ATTENTE_VALIDATION", regionId, entiteId, typeInfractionId, debut, fin);
        long brouillon = ficheRepo.countByStatutFiltre("BROUILLON", regionId, entiteId, typeInfractionId, debut, fin);
        long rejetees = ficheRepo.countByStatutFiltre("REJETE", regionId, entiteId, typeInfractionId, debut, fin);

        Map<String, Long> parTypeFiche = new LinkedHashMap<>();
        parTypeFiche.put("PERSONNE_PHYSIQUE", ppRepo.countFiltre(regionId, entiteId, typeInfractionId, debut, fin));
        parTypeFiche.put("PERSONNE_MORALE", pmRepo.countFiltre(regionId, entiteId, typeInfractionId, debut, fin));

        // countGroupByEntite / countGroupByTypeInfraction appelées une seule
        // fois chacune, réutilisées pour repartitionParametrable ET top5Entites
        // / parNatureInfraction (évite les doubles appels DB)
        Map<String, Long> parEntite = versMap(ficheRepo.countGroupByEntite(regionId, typeInfractionId, debut, fin));
        Map<String, Long> parInfraction = versMap(infractionRepo.countGroupByTypeInfraction(regionId, entiteId, debut, fin));

        Map<String, Long> repartitionParametrable = switch (groupBy) {
            case "ENTITE" -> parEntite;
            case "INFRACTION" -> parInfraction;
            default -> versMap(ficheRepo.countGroupByRegion(entiteId, typeInfractionId, debut, fin));
        };

        Map<String, Long> parStatutJudiciaire = versMap(
                ficheRepo.countGroupByStatutJudiciaire(regionId, entiteId, debut, fin));

        Map<String, Long> top5Entites = parEntite.entrySet().stream()
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        return StatistiqueResponse.builder()
                .totalFiches(total)
                .fichesActives(actives)
                .fichesEnAttente(enAttente)
                .fichesBrouillon(brouillon)
                .fichesRejetees(rejetees)
                .parTypeFiche(parTypeFiche)
                .groupBy(groupBy)
                .repartitionParametrable(repartitionParametrable)
                .parStatutJudiciaire(parStatutJudiciaire)
                .parNatureInfraction(parInfraction)
                .top5Entites(top5Entites)
                .build();
    }

    private Map<String, Long> versMap(List<Object[]> lignes) {
        Map<String, Long> resultat = new LinkedHashMap<>();
        for (Object[] ligne : lignes) {
            String cle = ligne[0] != null ? ligne[0].toString() : "NON_RENSEIGNE";
            Long valeur = (Long) ligne[1];
            resultat.put(cle, valeur);
        }
        return resultat;
    }
}