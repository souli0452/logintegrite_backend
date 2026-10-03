package bf.gov.ascelc.logintegrite_backend.dossier;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.audit.service.ConsultationService;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.DossierRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.FaitReproche;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutDossier;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.DossierMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.FaitReprocheRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.service.RegleDossier;
import bf.gov.ascelc.logintegrite_backend.dossier.service.impl.DossierServiceImpl;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.SourceSignalementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClotureDossierTest {

    private final UUID id = UUID.randomUUID();
    private DossierRepository repository;
    private FaitReprocheRepository faits;
    private DossierServiceImpl service;
    private Dossier dossier;

    @BeforeEach
    void preparer() {
        repository = mock(DossierRepository.class);
        faits = mock(FaitReprocheRepository.class);
        service = new DossierServiceImpl(repository, mock(SourceSignalementRepository.class), mock(DossierMapper.class),
                mock(AuditService.class), mock(ConsultationService.class), faits);
        dossier = new Dossier();
        when(repository.findById(id)).thenReturn(Optional.of(dossier));
        when(repository.save(any(Dossier.class))).thenAnswer(a -> a.getArgument(0));
    }

    private static FaitReproche fait(StatutValidation statut) {
        FaitReproche f = mock(FaitReproche.class);
        when(f.getStatutValidation()).thenReturn(statut);
        return f;
    }

    @Test
    void cloture_un_dossier_ouvert_dont_tous_les_faits_sont_traites() {
        FaitReproche valide = fait(StatutValidation.VALIDEE);
        FaitReproche rejete = fait(StatutValidation.REJETEE);
        when(faits.findByDossierId(id)).thenReturn(List.of(valide, rejete));

        service.cloturer(id);

        assertEquals(StatutDossier.CLOTURE, dossier.getStatutDossier());
        assertTrue(dossier.getDateCloture() != null);
        verify(repository).save(dossier);
    }

    @Test
    void refuse_la_cloture_tant_qu_un_fait_attend_une_validation() {
        FaitReproche enAttente = fait(StatutValidation.EN_ATTENTE);
        when(faits.findByDossierId(id)).thenReturn(List.of(enAttente));

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.cloturer(id));

        assertEquals(409, e.getStatusCode().value());
        assertEquals(StatutDossier.OUVERT, dossier.getStatutDossier());
        verify(repository, never()).save(any());
    }

    @Test
    void ne_cloture_pas_deux_fois() {
        dossier.setStatutDossier(StatutDossier.CLOTURE);
        assertThrows(ResponseStatusException.class, () -> service.cloturer(id));
    }

    @Test
    void un_dossier_cloture_n_est_plus_modifiable() {
        dossier.setStatutDossier(StatutDossier.CLOTURE);
        DossierRequest requete = new DossierRequest();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.modifier(id, requete));
        assertEquals(409, e.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void la_regle_commune_laisse_passer_un_dossier_ouvert() {
        RegleDossier.verifierOuvert(new Dossier());
    }
}
