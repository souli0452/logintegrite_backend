package bf.gov.ascelc.logintegrite_backend.audit.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.dto.request.EvenementPosteRequest;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.EvenementSecuriteResponse;
import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalSecurite;
import bf.gov.ascelc.logintegrite_backend.audit.repository.JournalSecuriteRepository;
import bf.gov.ascelc.logintegrite_backend.audit.service.JournalSecuriteService;
import bf.gov.ascelc.logintegrite_backend.common.security.ClientIp;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JournalSecuriteServiceImpl implements JournalSecuriteService {

    /** Types acceptes : le poste ne peut pas inventer de categories. */
    static final Set<String> TYPES = Set.of(
            "COPIE_TENTEE", "COUPE_TENTEE", "IMPRESSION_TENTEE", "MENU_CONTEXTUEL",
            "CAPTURE_SUSPECTE", "ONGLET_QUITTE", "OUTILS_DEVELOPPEUR");

    /** Au plus 60 evenements par utilisateur et par minute. */
    static final int PLAFOND_PAR_MINUTE = 60;

    private final JournalSecuriteRepository repository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void enregistrer(EvenementPosteRequest requete, String userAgent) {
        if (!TYPES.contains(requete.type())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type d'evenement inconnu");
        }
        Utilisateur utilisateur = currentUserProvider.utilisateurCourant();
        UUID id = utilisateur.getId();
        if (repository.countByUtilisateurIdAndDateEvenementAfter(id, Instant.now().minus(Duration.ofMinutes(1)))
                >= PLAFOND_PAR_MINUTE) {
            return; // rafale : on garde les premiers evenements, sans erreur cote client
        }
        JournalSecurite e = new JournalSecurite();
        e.setId(UUID.randomUUID());
        e.setDateEvenement(Instant.now());
        e.setUtilisateur(utilisateur);
        e.setTypeEvenement(requete.type());
        e.setPage(requete.page());
        e.setDetail(requete.detail());
        ClientIp.courante().ifPresent(e::setAdresseIp);
        e.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(400, userAgent.length())));
        repository.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EvenementSecuriteResponse> lister(String type, Pageable pageable) {
        Page<JournalSecurite> page = (type == null || type.isBlank())
                ? repository.findAllByOrderByDateEvenementDesc(pageable)
                : repository.findByTypeEvenementOrderByDateEvenementDesc(type, pageable);
        return page.map(e -> new EvenementSecuriteResponse(
                e.getId(), e.getDateEvenement(), e.getTypeEvenement(),
                e.getUtilisateur().getPrenom() + " " + e.getUtilisateur().getNom(),
                e.getPage(), e.getDetail(), e.getAdresseIp(), e.getUserAgent()));
    }
}
