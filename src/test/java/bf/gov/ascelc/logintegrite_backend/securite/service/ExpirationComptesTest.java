package bf.gov.ascelc.logintegrite_backend.securite.service;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpirationComptesTest {

    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 10, 2);

    // ---------------------------------------------------------------- regles

    @Test
    void un_compte_de_consultation_a_six_mois_par_defaut() {
        assertEquals(LocalDate.of(2027, 4, 2), RegleExpiration.pourCreation(CodeRole.CONSULTANT, null, AUJOURDHUI));
    }

    @Test
    void les_autres_roles_n_expirent_pas_par_defaut() {
        assertNull(RegleExpiration.pourCreation(CodeRole.AGENT, null, AUJOURDHUI));
        assertNull(RegleExpiration.pourCreation(CodeRole.ADMIN, null, AUJOURDHUI));
    }

    @Test
    void une_date_choisie_est_respectee_mais_pas_dans_le_passe() {
        LocalDate choisie = LocalDate.of(2026, 12, 31);
        assertEquals(choisie, RegleExpiration.pourCreation(CodeRole.CONSULTANT, choisie, AUJOURDHUI));
        assertThrows(ResponseStatusException.class,
                () -> RegleExpiration.pourCreation(CodeRole.AGENT, AUJOURDHUI.minusDays(1), AUJOURDHUI));
        // le jour meme reste valide : le compte expire le lendemain
        assertEquals(AUJOURDHUI, RegleExpiration.pourCreation(CodeRole.AGENT, AUJOURDHUI, AUJOURDHUI));
    }

    @Test
    void on_ne_retire_pas_l_expiration_d_un_compte_de_consultation() {
        assertThrows(ResponseStatusException.class, () -> RegleExpiration.verifierModification(true, null, AUJOURDHUI));
        RegleExpiration.verifierModification(false, null, AUJOURDHUI); // un agent : possible
        RegleExpiration.verifierModification(true, AUJOURDHUI.plusMonths(3), AUJOURDHUI);
    }

    // ---------------------------------------------------------------- controle a chaque requete

    private static Utilisateur compte(LocalDate expiration) {
        Utilisateur u = mock(Utilisateur.class);
        when(u.getDateExpiration()).thenReturn(expiration);
        return u;
    }

    @Test
    void un_compte_est_expire_le_lendemain_de_sa_date_pas_le_jour_meme() {
        UtilisateurRepository repo = mock(UtilisateurRepository.class);
        Utilisateur u = compte(AUJOURDHUI);
        when(repo.findByKeycloakId("kc")).thenReturn(Optional.of(u));
        ControleExpirationCompte controle = new ControleExpirationCompte(repo);
        assertFalse(controle.estExpire("kc", AUJOURDHUI));
        assertTrue(controle.estExpire("kc", AUJOURDHUI.plusDays(1)));
    }

    @Test
    void un_compte_sans_date_ou_inconnu_n_est_jamais_expire() {
        UtilisateurRepository repo = mock(UtilisateurRepository.class);
        Utilisateur sansDate = compte(null);
        when(repo.findByKeycloakId("sans")).thenReturn(Optional.of(sansDate));
        when(repo.findByKeycloakId("nouveau")).thenReturn(Optional.empty());
        ControleExpirationCompte controle = new ControleExpirationCompte(repo);
        assertFalse(controle.estExpire("sans", AUJOURDHUI));
        assertFalse(controle.estExpire("nouveau", AUJOURDHUI));
        assertFalse(controle.estExpire(null));
    }

    @Test
    void la_base_n_est_pas_interrogee_a_chaque_requete_et_la_memoire_s_invalide() {
        UtilisateurRepository repo = mock(UtilisateurRepository.class);
        Utilisateur ancien = compte(AUJOURDHUI.minusDays(5));
        when(repo.findByKeycloakId("kc")).thenReturn(Optional.of(ancien));
        ControleExpirationCompte controle = new ControleExpirationCompte(repo);
        assertTrue(controle.estExpire("kc", AUJOURDHUI));
        assertTrue(controle.estExpire("kc", AUJOURDHUI));
        assertTrue(controle.estExpire("kc", AUJOURDHUI));
        verify(repo, times(1)).findByKeycloakId("kc");

        // prolongation : apres invalidation, la nouvelle date est relue
        Utilisateur prolonge = compte(AUJOURDHUI.plusMonths(6));
        when(repo.findByKeycloakId("kc")).thenReturn(Optional.of(prolonge));
        controle.invalider("kc");
        assertFalse(controle.estExpire("kc", AUJOURDHUI));
        verify(repo, times(2)).findByKeycloakId("kc");
    }

    // ---------------------------------------------------------------- tache de desactivation

    @Test
    void la_tache_desactive_les_comptes_expires_dans_keycloak_et_en_base_et_trace() {
        UtilisateurRepository repo = mock(UtilisateurRepository.class);
        KeycloakAdminService keycloak = mock(KeycloakAdminService.class);
        ControleExpirationCompte controle = mock(ControleExpirationCompte.class);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Utilisateur u = mock(Utilisateur.class);
        when(u.getKeycloakId()).thenReturn("kc-1");
        java.util.UUID id = java.util.UUID.randomUUID();
        when(u.getId()).thenReturn(id);
        when(u.getDateExpiration()).thenReturn(AUJOURDHUI.minusDays(1));
        when(repo.findByActifTrueAndDateExpirationBefore(AUJOURDHUI)).thenReturn(List.of(u));

        int n = new ExpirationComptesJob(repo, keycloak, controle, jdbc).desactiver(AUJOURDHUI);

        assertEquals(1, n);
        verify(keycloak).modifierActivation("kc-1", false);
        verify(u).setActif(false);
        verify(controle).invalider("kc-1");
        verify(jdbc).update(any(String.class), any(), eq(id), eq("COMPTE_EXPIRE"), any(), any());
    }

    @Test
    void un_echec_keycloak_ne_bloque_pas_les_autres_comptes_et_ne_desactive_pas_localement() {
        UtilisateurRepository repo = mock(UtilisateurRepository.class);
        KeycloakAdminService keycloak = mock(KeycloakAdminService.class);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Utilisateur en_panne = mock(Utilisateur.class);
        Utilisateur bon = mock(Utilisateur.class);
        when(en_panne.getKeycloakId()).thenReturn("kc-panne");
        when(bon.getKeycloakId()).thenReturn("kc-bon");
        when(bon.getId()).thenReturn(java.util.UUID.randomUUID());
        doThrow(new IllegalStateException("Keycloak injoignable")).when(keycloak).modifierActivation("kc-panne", false);
        when(repo.findByActifTrueAndDateExpirationBefore(AUJOURDHUI)).thenReturn(List.of(en_panne, bon));

        int n = new ExpirationComptesJob(repo, keycloak, mock(ControleExpirationCompte.class), jdbc).desactiver(AUJOURDHUI);

        assertEquals(1, n);
        verify(en_panne, never()).setActif(anyBoolean());
        verify(bon).setActif(false);
    }
}
