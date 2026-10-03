package bf.gov.ascelc.logintegrite_backend.personne;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.audit.service.ConsultationService;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.VerificationNipResponse;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.personne.mapper.PersonnePhysiqueMapper;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonnePhysiqueRepository;
import bf.gov.ascelc.logintegrite_backend.personne.service.impl.PersonnePhysiqueServiceImpl;
import bf.gov.ascelc.logintegrite_backend.personne.util.NipUtil;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.NationaliteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NipTest {

    private static final String NIP = "12345678901234567";

    // ---------------------------------------------------------------- format

    @Test
    void normalise_les_espaces_et_les_minuscules() {
        assertEquals("ABC12345678901234", NipUtil.normaliser("  abc 1234 5678 9012 34 "));
        assertNull(NipUtil.normaliser("   "));
        assertNull(NipUtil.normaliser(null));
    }

    @Test
    void exige_exactement_17_caracteres_alphanumeriques() {
        assertTrue(NipUtil.estValide(NIP));
        assertTrue(NipUtil.estValide("AB12CD34EF56GH78I"));
        assertFalse(NipUtil.estValide("1234567890123456"));      // 16
        assertFalse(NipUtil.estValide("123456789012345678"));    // 18
        assertFalse(NipUtil.estValide("1234567890123456-"));     // caractere special
        assertFalse(NipUtil.estValide("abcdefghijklmnopq"));     // minuscules : doit etre normalise avant
        assertFalse(NipUtil.estValide(null));
    }

    // ---------------------------------------------------------------- service

    private PersonnePhysiqueRepository repository;
    private PersonnePhysiqueMapper mapper;
    private PersonnePhysiqueServiceImpl service;

    @BeforeEach
    void preparer() {
        repository = mock(PersonnePhysiqueRepository.class);
        mapper = mock(PersonnePhysiqueMapper.class);
        service = new PersonnePhysiqueServiceImpl(repository, mock(NationaliteRepository.class),
                mock(CurrentUserProvider.class), mapper, mock(AuditService.class), mock(ConsultationService.class));
    }

    private static PersonnePhysique porteur(UUID id) {
        PersonnePhysique p = mock(PersonnePhysique.class);
        when(p.getId()).thenReturn(id);
        when(p.getNomAffichage()).thenReturn("OUEDRAOGO Salif");
        when(p.getNumeroPersonne()).thenReturn("PERS-2026-00007");
        return p;
    }

    @Test
    void un_nip_libre_est_signale_disponible() {
        when(repository.findByNip(NIP)).thenReturn(Optional.empty());
        assertTrue(service.verifierNip(NIP, null).disponible());
    }

    @Test
    void un_nip_deja_porte_designe_la_personne() {
        UUID id = UUID.randomUUID();
        PersonnePhysique autre = porteur(id);
        when(repository.findByNip(NIP)).thenReturn(Optional.of(autre));

        VerificationNipResponse r = service.verifierNip(" 1234 5678 9012 3456 7 ", null);

        assertFalse(r.disponible());
        assertEquals(id, r.personneExistanteId());
        assertEquals("OUEDRAOGO Salif", r.personneExistanteNomAffichage());
    }

    @Test
    void la_personne_qu_on_modifie_ne_compte_pas_comme_un_doublon_d_elle_meme() {
        UUID id = UUID.randomUUID();
        PersonnePhysique elle = porteur(id);
        when(repository.findByNip(NIP)).thenReturn(Optional.of(elle));
        assertTrue(service.verifierNip(NIP, id).disponible());
    }

    @Test
    void un_nip_mal_forme_est_refuse_en_400() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.verifierNip("123", null));
        assertEquals(400, e.getStatusCode().value());
    }

    @Test
    void la_creation_refuse_un_nip_deja_enregistre_en_409_sans_rien_sauvegarder() {
        PersonnePhysique autre = porteur(UUID.randomUUID());
        when(repository.findByNip(NIP)).thenReturn(Optional.of(autre));
        PersonnePhysiqueRequest requete = new PersonnePhysiqueRequest();
        requete.setNip(NIP);
        when(mapper.toEntity(requete)).thenReturn(new PersonnePhysique());

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.creer(requete));

        assertEquals(409, e.getStatusCode().value());
        assertTrue(e.getReason().contains("PERS-2026-00007"));
        verify(repository, never()).save(any());
    }

    @Test
    void la_creation_normalise_et_enregistre_le_nip() {
        when(repository.findByNip(NIP)).thenReturn(Optional.empty());
        PersonnePhysique entite = new PersonnePhysique();
        PersonnePhysiqueRequest requete = new PersonnePhysiqueRequest();
        requete.setNip(" 1234 5678 9012 3456 7 ");
        when(mapper.toEntity(requete)).thenReturn(entite);
        when(repository.save(entite)).thenReturn(entite);

        try {
            service.creer(requete);
        } catch (NullPointerException ignore) {
            // l'audit et la reponse sont des doublures : seul le NIP nous interesse ici
        }
        assertEquals(NIP, entite.getNip());
    }
}
