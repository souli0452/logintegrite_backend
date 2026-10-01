package bf.gov.ascelc.logintegrite_backend.audit.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.dto.request.EvenementPosteRequest;
import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalSecurite;
import bf.gov.ascelc.logintegrite_backend.audit.repository.JournalSecuriteRepository;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JournalSecuriteServiceImplTest {

    private JournalSecuriteRepository repository;
    private JournalSecuriteServiceImpl service;
    private Utilisateur utilisateur;

    @BeforeEach
    void preparer() {
        repository = mock(JournalSecuriteRepository.class);
        CurrentUserProvider courant = mock(CurrentUserProvider.class);
        utilisateur = mock(Utilisateur.class);
        when(utilisateur.getId()).thenReturn(UUID.randomUUID());
        when(courant.utilisateurCourant()).thenReturn(utilisateur);
        service = new JournalSecuriteServiceImpl(repository, courant);
    }

    @Test
    void refuse_un_type_inconnu() {
        assertThrows(ResponseStatusException.class,
                () -> service.enregistrer(new EvenementPosteRequest("N_IMPORTE_QUOI", "/x", null), "ua"));
        verify(repository, never()).save(any());
    }

    @Test
    void enregistre_un_evenement_valide_avec_l_utilisateur_courant() {
        when(repository.countByUtilisateurIdAndDateEvenementAfter(any(), any(Instant.class))).thenReturn(0L);
        service.enregistrer(new EvenementPosteRequest("COPIE_TENTEE", "/personnes", "selection"), "Mozilla");
        var capteur = org.mockito.ArgumentCaptor.forClass(JournalSecurite.class);
        verify(repository).save(capteur.capture());
        assertEquals("COPIE_TENTEE", capteur.getValue().getTypeEvenement());
        assertSame(utilisateur, capteur.getValue().getUtilisateur());
        assertEquals("Mozilla", capteur.getValue().getUserAgent());
    }

    @Test
    void ignore_la_rafale_au_dela_du_plafond() {
        when(repository.countByUtilisateurIdAndDateEvenementAfter(any(), any(Instant.class)))
                .thenReturn((long) JournalSecuriteServiceImpl.PLAFOND_PAR_MINUTE);
        service.enregistrer(new EvenementPosteRequest("COPIE_TENTEE", "/x", null), "ua");
        verify(repository, never()).save(any());
    }
}
