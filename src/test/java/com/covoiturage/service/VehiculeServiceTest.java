package com.covoiturage.service;

import com.covoiturage.domain.entity.Conducteur;
import com.covoiturage.domain.entity.Vehicule;
import com.covoiturage.domain.repository.ConducteurRepository;
import com.covoiturage.domain.repository.VehiculeRepository;
import com.covoiturage.dto.request.VehiculeCreateRequest;
import com.covoiturage.dto.response.VehiculeResponse;
import com.covoiturage.exception.BusinessConflictException;
import com.covoiturage.exception.BusinessValidationException;
import com.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.mapper.VehiculeMapper;
import com.covoiturage.service.impl.VehiculeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceTest {

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private VehiculeMapper vehiculeMapper;

    @InjectMocks
    private VehiculeServiceImpl vehiculeService;

    private Conducteur hamid;
    private Vehicule daciaLogan;

    @BeforeEach
    void setUp() {
        hamid = Conducteur.builder().id(1L).nom("El Amrani").prenom("Hamid").build();
        daciaLogan = Vehicule.builder().id(10L).immatriculation("12345-A-1").capacite(4).conducteur(hamid).build();
    }

    @Test
    @DisplayName("Validation : capacité supérieure à 8 refusée -> 400")
    void shouldRejectCapacityAbove8() {
        VehiculeCreateRequest request = new VehiculeCreateRequest(
                "Dacia", "Logan", "12345-A-1", "Gris", 9, 1L
        );

        assertThatThrownBy(() -> vehiculeService.addVehicule(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("comprise entre 1 et 8 places");
    }

    @Test
    @DisplayName("Règle : suppression refusée lorsqu'un véhicule est associé à des trajets")
    void shouldRejectDeletionWhenAssociatedWithTrips() {
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(daciaLogan));
        when(vehiculeRepository.isVehiculeAssociatedWithTrajets(10L)).thenReturn(true);

        assertThatThrownBy(() -> vehiculeService.deleteVehicule(10L, 1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("associé à un ou plusieurs trajets");

        verify(vehiculeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Matrice des actions : un conducteur ne peut pas supprimer le véhicule d'un autre conducteur -> 403")
    void shouldRejectDeletionByAnotherDriver() {
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(daciaLogan));

        assertThatThrownBy(() -> vehiculeService.deleteVehicule(10L, 99L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("Vous ne pouvez supprimer que vos propres véhicules");
    }
}
