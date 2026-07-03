package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatistiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.StatistiqueResponse;
import bf.gov.ascelc.logintegrite_backend.entity.EntiteOrganisation;
import bf.gov.ascelc.logintegrite_backend.entity.Region;
import bf.gov.ascelc.logintegrite_backend.entity.TypeInfraction;
import bf.gov.ascelc.logintegrite_backend.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.repository.InfractionRepository;
import bf.gov.ascelc.logintegrite_backend.repository.PersonneMoraleRepository;
import bf.gov.ascelc.logintegrite_backend.repository.PersonnePhysiqueRepository;
import bf.gov.ascelc.logintegrite_backend.repository.RegionRepository;
import bf.gov.ascelc.logintegrite_backend.repository.TypeInfractionRepository;
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

    // AJOUT : nécessaires pour lister l'intégralité des référentiels
    // existants, indépendamment du fait qu'ils aient déjà des fiches ou non.
    private final RegionRepository regionRepo;
    private final EntiteOrganisationRepository entiteRepo;
    private final TypeInfractionRepository typeInfractionRepo;

    @Override
    @Transactional(readOnly = true)
    public StatistiqueResponse calculer(StatistiqueRequest filtres) {

        String statut = filtres.getStatut();
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

        // MODIFIÉ (statistiques "intelligentes") : les requêtes de comptage
        // (countGroupByRegion/Entite/TypeInfraction) partent des fiches et
        // ne renvoient donc que les référentiels déjà utilisés. Un référentiel
        // fraîchement créé par l'admin, sans fiche associée, n'apparaissait
        // pas du tout dans l'histogramme. On part maintenant de la liste
        // complète des référentiels (initialisés à 0), puis on superpose les
        // comptes réels — un nouveau référentiel apparaît donc immédiatement,
        // à zéro, sans attendre qu'une fiche lui soit rattachée.
        List<String> toutesRegions = regionRepo.findByActifTrue().stream()
                .map(Region::getNom).collect(Collectors.toList());
        List<String> toutesEntites = entiteRepo.findByActifTrue().stream()
                .map(EntiteOrganisation::getNom).collect(Collectors.toList());
        // TypeInfraction n'a pas de notion actif/inactif dans le modèle actuel
        // (pas de champ 'actif' sur cette entité) : on liste donc tous les types.
        List<String> tousTypesInfraction = typeInfractionRepo.findAll().stream()
                .map(TypeInfraction::getLibelle).collect(Collectors.toList());

        Map<String, Long> parRegion = avecReferentielsComplets(toutesRegions,
                ficheRepo.countGroupByRegion(entiteId, typeInfractionId, debut, fin));
        Map<String, Long> parEntite = avecReferentielsComplets(toutesEntites,
                ficheRepo.countGroupByEntite(regionId, typeInfractionId, debut, fin));
        Map<String, Long> parInfraction = avecReferentielsComplets(tousTypesInfraction,
                infractionRepo.countGroupByTypeInfraction(regionId, entiteId, debut, fin));

        Map<String, Long> repartitionParametrable = switch (groupBy) {
            case "ENTITE" -> trierParValeurDecroissante(parEntite);
            case "INFRACTION" -> trierParValeurDecroissante(parInfraction);
            default -> trierParValeurDecroissante(parRegion);
        };

        Map<String, Long> parStatutJudiciaire = versMap(
                ficheRepo.countGroupByStatutJudiciaire(regionId, entiteId, debut, fin));

        // top5Entites reste basé sur les comptes réels uniquement (un "top 5"
        // n'a pas de sens rempli d'entités à zéro fiche)
        Map<String, Long> top5Entites = versMap(ficheRepo.countGroupByEntite(regionId, typeInfractionId, debut, fin))
                .entrySet().stream()
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
                .parNatureInfraction(trierParValeurDecroissante(parInfraction))
                .top5Entites(top5Entites)
                .build();
    }

    // AJOUT : initialise chaque référentiel connu à 0, puis superpose les
    // comptes réels issus de la requête de regroupement.
    private Map<String, Long> avecReferentielsComplets(List<String> tousLesLibelles, List<Object[]> lignesComptees) {
        Map<String, Long> resultat = new LinkedHashMap<>();
        tousLesLibelles.forEach(libelle -> resultat.put(libelle, 0L));
        for (Object[] ligne : lignesComptees) {
            if (ligne[0] != null) {
                resultat.put(ligne[0].toString(), (Long) ligne[1]);
            }
        }
        return resultat;
    }

    // AJOUT : tri décroissant par valeur pour un affichage d'histogramme
    // cohérent (les référentiels actifs en tête, les référentiels encore
    // vides en bas, mais toujours visibles).
    private Map<String, Long> trierParValeurDecroissante(Map<String, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
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