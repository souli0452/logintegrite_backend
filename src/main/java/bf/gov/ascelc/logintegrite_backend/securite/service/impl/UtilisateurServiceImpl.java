package bf.gov.ascelc.logintegrite_backend.securite.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.securite.dto.request.UtilisateurCreationRequest;
import bf.gov.ascelc.logintegrite_backend.securite.dto.response.RoleHabilitationResponse;
import bf.gov.ascelc.logintegrite_backend.securite.dto.response.UtilisateurResponse;
import bf.gov.ascelc.logintegrite_backend.securite.entity.RoleHabilitation;
import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import bf.gov.ascelc.logintegrite_backend.securite.entity.UtilisateurRole;
import bf.gov.ascelc.logintegrite_backend.securite.mapper.RoleHabilitationMapper;
import bf.gov.ascelc.logintegrite_backend.securite.repository.RoleHabilitationRepository;
import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRepository;
import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRoleRepository;
import bf.gov.ascelc.logintegrite_backend.securite.service.KeycloakAdminService;
import bf.gov.ascelc.logintegrite_backend.securite.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleHabilitationRepository roleHabilitationRepository;
    private final UtilisateurRoleRepository utilisateurRoleRepository;
    private final RoleHabilitationMapper roleHabilitationMapper;
    private final AuditService auditService;
    private final KeycloakAdminService keycloakAdmin;   // NOUVEAU

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> lister() {
        return utilisateurRepository.findAll().stream().map(this::versReponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponse obtenir(UUID id) {
        return versReponse(trouverOuLever(id));
    }

    /**
     * NOUVEAU : Cree un utilisateur dans Keycloak puis en base, avec attribution du role initial.
     */
    @Override
    public UtilisateurResponse creer(UtilisateurCreationRequest request) {
        // Verifier que l'email n'existe pas deja en base
        if (utilisateurRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalStateException("Un utilisateur avec cet email existe deja : " + request.getEmail());
        }

        // Trouver le RoleHabilitation correspondant au CodeRole demande
        RoleHabilitation role = roleHabilitationRepository.findByCode(request.getRoleInitial())
        .orElseThrow(() -> new IllegalStateException(
                "Role d'habilitation non trouve pour le code : " + request.getRoleInitial()
                + ". Assurez-vous que la table 'role_habilitation' contient ce code."));

        // Etape 1 : Creer dans Keycloak (recupere l'id Keycloak)
        String keycloakId = keycloakAdmin.creerUtilisateur(
                request.getNom(), request.getPrenom(), request.getEmail(),
                request.getMotDePasseTemporaire(), request.getRoleInitial());

        // Etape 2 : Creer l'entree locale
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setKeycloakId(keycloakId);
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setActif(true);
        utilisateur = utilisateurRepository.save(utilisateur);

        // Etape 3 : Attribuer le role en base
        UtilisateurRole lien = new UtilisateurRole();
        lien.setUtilisateur(utilisateur);
        lien.setRoleHabilitation(role);
        utilisateurRoleRepository.save(lien);

        auditService.enregistrer("CREATION_UTILISATEUR", "Utilisateur", utilisateur.getId(), null,
                Map.of("email", request.getEmail(), "role", request.getRoleInitial().name()));

        log.info("Utilisateur cree : id={}, email={}, role={}",
                utilisateur.getId(), request.getEmail(), request.getRoleInitial());

        return versReponse(utilisateur);
    }

    /**
     * NOUVEAU : Active ou desactive un utilisateur (Keycloak + local).
     */
    @Override
    public UtilisateurResponse modifierActivation(UUID id, boolean actif) {
        Utilisateur utilisateur = trouverOuLever(id);
        keycloakAdmin.modifierActivation(utilisateur.getKeycloakId(), actif);
        utilisateur.setActif(actif);
        utilisateur = utilisateurRepository.save(utilisateur);

        auditService.enregistrer(actif ? "ACTIVATION_UTILISATEUR" : "DESACTIVATION_UTILISATEUR",
                "Utilisateur", id, null, Map.of("actif", actif));

        return versReponse(utilisateur);
    }

    /**
     * NOUVEAU : Suppression complete (Keycloak + local + roles).
     */
    @Override
    public void supprimer(UUID id) {
        Utilisateur utilisateur = trouverOuLever(id);
        String keycloakId = utilisateur.getKeycloakId();

        // Suppression des liens de roles
        utilisateurRoleRepository.deleteAll(utilisateurRoleRepository.findByUtilisateurId(id));

        // Suppression en base
        utilisateurRepository.delete(utilisateur);

        // Suppression Keycloak (non bloquant)
        keycloakAdmin.supprimerUtilisateur(keycloakId);

        auditService.enregistrer("SUPPRESSION_UTILISATEUR", "Utilisateur", id,
                Map.of("email", utilisateur.getEmail()), null);
    }

    @Override
    public UtilisateurResponse attribuerRole(UUID utilisateurId, UUID roleHabilitationId) {
        Utilisateur utilisateur = trouverOuLever(utilisateurId);
        RoleHabilitation role = roleHabilitationRepository.findById(roleHabilitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Role habilitation", roleHabilitationId));

        if (!utilisateurRoleRepository.existsByUtilisateur_IdAndRoleHabilitation_Id(utilisateurId, roleHabilitationId)) {
            UtilisateurRole lien = new UtilisateurRole();
            lien.setUtilisateur(utilisateur);
            lien.setRoleHabilitation(role);
            utilisateurRoleRepository.save(lien);

            // Propagation dans Keycloak
            keycloakAdmin.attribuerRole(utilisateur.getKeycloakId(), role.getCode());

            auditService.enregistrer("ATTRIBUTION_ROLE", "Utilisateur", utilisateurId, null,
                    Map.of("role", role.getCode().name()));
        }
        return versReponse(utilisateur);
    }

    @Override
    public UtilisateurResponse retirerRole(UUID utilisateurId, UUID roleHabilitationId) {
        Utilisateur utilisateur = trouverOuLever(utilisateurId);
        RoleHabilitation role = roleHabilitationRepository.findById(roleHabilitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Role habilitation", roleHabilitationId));

        utilisateurRoleRepository.deleteByUtilisateur_IdAndRoleHabilitation_Id(utilisateurId, roleHabilitationId);

        // Propagation dans Keycloak
        keycloakAdmin.retirerRole(utilisateur.getKeycloakId(), role.getCode());

        auditService.enregistrer("RETRAIT_ROLE", "Utilisateur", utilisateurId,
                Map.of("roleHabilitationId", roleHabilitationId.toString()), null);

        return versReponse(utilisateur);
    }

    private Utilisateur trouverOuLever(UUID id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    private UtilisateurResponse versReponse(Utilisateur u) {
        List<RoleHabilitationResponse> roles = utilisateurRoleRepository.findByUtilisateurId(u.getId())
                .stream().map(ur -> roleHabilitationMapper.toResponse(ur.getRoleHabilitation())).toList();
        return UtilisateurResponse.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .actif(u.isActif())
                .roles(roles)
                .build();
    }
}
