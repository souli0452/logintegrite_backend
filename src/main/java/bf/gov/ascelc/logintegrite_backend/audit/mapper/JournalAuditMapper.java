package bf.gov.ascelc.logintegrite_backend.audit.mapper;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.JournalAuditResponse;
import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class JournalAuditMapper {

    private final JsonMapper jsonMapper;

    public JournalAuditResponse toResponse(JournalAudit entity) {
        return JournalAuditResponse.builder()
                .id(entity.getId())
                .action(entity.getAction())
                .entiteCible(entity.getEntiteCible())
                .entiteCibleId(entity.getEntiteCibleId())
                .valeurAvant(depuisJson(entity.getValeurAvant()))
                .valeurApres(depuisJson(entity.getValeurApres()))
                .hashPrecedent(entity.getHashPrecedent())
                .hashActuel(entity.getHashActuel())
                .dateAction(entity.getDateAction())
                .adresseIp(entity.getAdresseIp())
                .utilisateurId(entity.getUtilisateur() != null ? entity.getUtilisateur().getId() : null)
                .utilisateurNomComplet(entity.getUtilisateur() != null
                        ? entity.getUtilisateur().getPrenom() + " " + entity.getUtilisateur().getNom()
                        : null)
                .build();
    }

    // Les colonnes JSON sont stockees en base comme du texte JSON brut -
    // on les redeserialise en Object pour que Jackson les reserialise comme
    // un vrai noeud JSON imbrique dans la reponse, jamais comme une chaine
    // echappee (ce que le frontend attend deja via son typage `unknown`).
    private Object depuisJson(String json) {
        if (json == null) return null;
        try {
            return jsonMapper.readValue(json, Object.class);
        } catch (RuntimeException e) {
            return json;
        }
    }
}
