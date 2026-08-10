package bf.gov.ascelc.logintegrite_backend.audit.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.EtatChaineResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.VerificationChaineResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.VerificationMaillonResponse;
import bf.gov.ascelc.logintegrite_backend.audit.repository.JournalAuditForensiqueRepository;
import bf.gov.ascelc.logintegrite_backend.audit.service.AuditIntegriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du service d'intégrité.
 *
 * <p>Toutes les opérations sont exécutées en transaction {@code readOnly = true}
 * — aucune modification du journal d'audit n'est autorisée depuis ce service.
 * La logique métier reste minimale : le vrai travail est délégué aux fonctions
 * PostgreSQL du schéma {@code audit}, ce qui garantit que la formule de hash
 * n'existe qu'à un seul endroit dans tout le système.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuditIntegriteServiceImpl implements AuditIntegriteService {

    private final JournalAuditForensiqueRepository repository;

    // -------------------------------------------------------------------------
    // 1. État de la chaîne
    // -------------------------------------------------------------------------
    @Override
    public EtatChaineResponse etatChaine() {
        Map<String, Object> ligne = repository.etatChaine();
        if (ligne == null) {
            return EtatChaineResponse.builder()
                    .totalEntrees(0L)
                    .totalUtilisateurs(0L)
                    .totalEntites(0L)
                    .build();
        }
        return EtatChaineResponse.builder()
                .totalEntrees(lireLong(ligne, "total_entrees"))
                .dernierHash((String) ligne.get("dernier_hash"))
                .dateDerniereAction(lireInstant(ligne, "date_derniere_action"))
                .datePremiereAction(lireInstant(ligne, "date_premiere_action"))
                .totalUtilisateurs(lireLong(ligne, "total_utilisateurs"))
                .totalEntites(lireLong(ligne, "total_entites"))
                .build();
    }

    // -------------------------------------------------------------------------
    // 2. Vérification complète de la chaîne
    // -------------------------------------------------------------------------
    @Override
    public VerificationChaineResponse verifierChaineComplete(Long limiteLignes) {
        long debut = System.currentTimeMillis();
        List<Map<String, Object>> lignes = repository.verifierIntegriteChaine(limiteLignes);
        long duree = System.currentTimeMillis() - debut;

        List<VerificationChaineResponse.MaillonRompu> ruptures = new ArrayList<>(lignes.size());
        for (Map<String, Object> l : lignes) {
            ruptures.add(VerificationChaineResponse.MaillonRompu.builder()
                    .id(lireUuid(l, "id"))
                    .dateAction(lireInstant(l, "date_action"))
                    .action((String) l.get("action"))
                    .entiteCible((String) l.get("entite_cible"))
                    .hashAttendu((String) l.get("hash_attendu"))
                    .hashStocke((String) l.get("hash_stocke"))
                    .hashPrecedent((String) l.get("hash_precedent"))
                    .hashPrecedentAttendu((String) l.get("hash_precedent_attendu"))
                    .typeRupture((String) l.get("type_rupture"))
                    .build());
        }

        long maillonsVerifies = (limiteLignes != null)
                ? limiteLignes
                : repository.etatChaine() == null
                    ? 0L
                    : lireLong(repository.etatChaine(), "total_entrees");

        if (!ruptures.isEmpty()) {
            log.warn("Vérification chaîne audit : {} ruptures détectées sur {} maillons ({} ms)",
                    ruptures.size(), maillonsVerifies, duree);
        } else {
            log.info("Vérification chaîne audit : chaîne intacte sur {} maillons ({} ms)",
                    maillonsVerifies, duree);
        }

        return VerificationChaineResponse.builder()
                .chaineIntegre(ruptures.isEmpty())
                .maillonsVerifies(maillonsVerifies)
                .nombreRuptures(ruptures.size())
                .dateVerification(Instant.now())
                .dureeMillisecondes(duree)
                .maillonsRompus(ruptures)
                .build();
    }

    // -------------------------------------------------------------------------
    // 3. Vérification d'un maillon isolé
    // -------------------------------------------------------------------------
    @Override
    public Optional<VerificationMaillonResponse> verifierMaillonParHash(String hash) {
        if (hash == null || hash.isBlank()) {
            return Optional.empty();
        }
        Map<String, Object> ligne = repository.verifierMaillon(hash.trim().toLowerCase());
        if (ligne == null || ligne.get("id") == null) {
            return Optional.empty();
        }
        return Optional.of(VerificationMaillonResponse.builder()
                .id(lireUuid(ligne, "id"))
                .dateAction(lireInstant(ligne, "date_action"))
                .action((String) ligne.get("action"))
                .entiteCible((String) ligne.get("entite_cible"))
                .hashStocke((String) ligne.get("hash_stocke"))
                .hashRecalcule((String) ligne.get("hash_recalcule"))
                .hashPrecedent((String) ligne.get("hash_precedent"))
                .integre(Boolean.TRUE.equals(ligne.get("integre")))
                .positionDansChaine(lireLong(ligne, "position_dans_chaine"))
                .build());
    }

    // -------------------------------------------------------------------------
    // Utilitaires de lecture typée depuis Map<String, Object>
    // -------------------------------------------------------------------------
    private long lireLong(Map<String, Object> ligne, String cle) {
        Object v = ligne.get(cle);
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }

    private UUID lireUuid(Map<String, Object> ligne, String cle) {
        Object v = ligne.get(cle);
        if (v == null) return null;
        if (v instanceof UUID u) return u;
        return UUID.fromString(v.toString());
    }

    private Instant lireInstant(Map<String, Object> ligne, String cle) {
        Object v = ligne.get(cle);
        if (v == null) return null;
        if (v instanceof Timestamp t) return t.toInstant();
        if (v instanceof Instant i) return i;
        return Instant.parse(v.toString());
    }
}
