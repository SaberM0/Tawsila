package com.covoiturage.service;

import com.covoiturage.domain.entity.Conducteur;
import com.covoiturage.domain.entity.PointArret;
import com.covoiturage.domain.entity.Reservation;
import com.covoiturage.domain.entity.Trajet;
import com.covoiturage.domain.entity.Vehicule;
import com.covoiturage.domain.enums.StatutReservation;
import com.covoiturage.domain.enums.StatutTrajet;
import com.covoiturage.domain.repository.ConducteurRepository;
import com.covoiturage.domain.repository.ReservationRepository;
import com.covoiturage.domain.repository.TrajetRepository;
import com.covoiturage.domain.repository.VehiculeRepository;
import com.covoiturage.dto.request.PointArretRequest;
import com.covoiturage.dto.request.TrajetCreateRequest;
import com.covoiturage.dto.response.TrajetDetailResponse;
import com.covoiturage.exception.BusinessConflictException;
import com.covoiturage.exception.BusinessValidationException;
import com.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.mapper.ConducteurMapper;
import com.covoiturage.mapper.PointArretMapper;
import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.mapper.TrajetMapper;
import com.covoiturage.mapper.VehiculeMapper;
import com.covoiturage.service.impl.TrajetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrajetServiceTest {

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TrajetMapper trajetMapper;

    @Mock
    private PointArretMapper pointArretMapper;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private ConducteurMapper conducteurMapper;

    @Mock
    private VehiculeMapper vehiculeMapper;

    @InjectMocks
    private TrajetServiceImpl trajetService;

    private Conducteur hamid;
    private Conducteur autreConducteur;
    private Vehicule daciaLogan;
    private Vehicule vehiculeAutre;
    private LocalDateTime departureDate;

    @BeforeEach
    void setUp() {
        departureDate = LocalDateTime.now().plusDays(2).withHour(17).withMinute(0);

        hamid = Conducteur.builder()
                .id(1L)
                .nom("El Amrani")
                .prenom("Hamid")
                .email("hamid@covoiturage.ma")
                .telephone("0661123456")
                .numeroPermis("PERMIS-1001")
                .build();

        autreConducteur = Conducteur.builder()
                .id(2L)
                .nom("Benjelloun")
                .prenom("Youssef")
                .email("youssef@covoiturage.ma")
                .telephone("0662234567")
                .numeroPermis("PERMIS-1002")
                .build();

        daciaLogan = Vehicule.builder()
                .id(10L)
                .marque("Dacia")
                .modele("Logan")
                .immatriculation("12345-A-1")
                .couleur("Gris")
                .capacite(4)
                .conducteur(hamid)
                .build();

        vehiculeAutre = Vehicule.builder()
                .id(20L)
                .marque("Renault")
                .modele("Clio")
                .immatriculation("67890-B-6")
                .couleur("Blanc")
                .capacite(4)
                .conducteur(autreConducteur)
                .build();
    }

    private List<PointArretRequest> createHamidRoutePoints() {
        return List.of(
                new PointArretRequest("Rabat", 0, departureDate, new BigDecimal("0.00")),
                new PointArretRequest("Casablanca", 1, departureDate.plusMinutes(75), new BigDecimal("40.00")),
                new PointArretRequest("Settat", 2, departureDate.plusMinutes(135), new BigDecimal("70.00")),
                new PointArretRequest("Marrakech", 3, departureDate.plusMinutes(270), new BigDecimal("120.00"))
        );
    }

    @Test
    @DisplayName("Cas nominal : Hamid publie avec succès un trajet multi-étapes avec 3 places")
    void shouldPublishTrajetSuccessfully() {
        TrajetCreateRequest request = new TrajetCreateRequest(
                1L, 10L, 3, createHamidRoutePoints()
        );

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(hamid));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(daciaLogan));
        when(pointArretMapper.toEntity(any())).thenAnswer(invocation -> {
            PointArretRequest req = invocation.getArgument(0);
            return PointArret.builder()
                    .ville(req.ville())
                    .ordre(req.ordre())
                    .heurePassage(req.heurePassage())
                    .prixDepuisDepart(req.prixDepuisDepart())
                    .build();
        });
        when(trajetRepository.save(any(Trajet.class))).thenAnswer(invocation -> {
            Trajet t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });
        when(reservationRepository.countConfirmedSeatsByTrajetId(100L)).thenReturn(0);
        when(trajetMapper.toDetailResponse(any(), eq(3))).thenReturn(
                new TrajetDetailResponse(100L, "Rabat", "Marrakech", departureDate, 3, 3,
                        StatutTrajet.PUBLIE, null, null, List.of())
        );

        TrajetDetailResponse response = trajetService.publishTrajet(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.placesProposees()).isEqualTo(3);
        assertThat(response.placesRestantes()).isEqualTo(3);
        verify(trajetRepository, times(1)).save(any(Trajet.class));
    }

    @Test
    @DisplayName("Scénario 7 : Hamid propose 5 places dans un véhicule d'une capacité de 4 -> 400 ; 4 places au maximum")
    void shouldRejectWhenProposedSeatsExceedVehicleCapacity() {
        TrajetCreateRequest request = new TrajetCreateRequest(
                1L, 10L, 5, createHamidRoutePoints()
        );

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(hamid));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(daciaLogan));

        assertThatThrownBy(() -> trajetService.publishTrajet(request, 1L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("4 places au maximum");

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Scénario 8 : Hamid publie avec le véhicule d'un autre conducteur -> 403")
    void shouldRejectWhenPublishingWithAnotherDriversVehicle() {
        TrajetCreateRequest request = new TrajetCreateRequest(
                1L, 20L, 3, createHamidRoutePoints()
        );

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(hamid));
        when(vehiculeRepository.findById(20L)).thenReturn(Optional.of(vehiculeAutre));

        assertThatThrownBy(() -> trajetService.publishTrajet(request, 1L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("Le conducteur ne peut publier qu'avec un de ses propres véhicules");

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Scénario 9 : Hamid termine le trajet avant 17:00 le jour du départ -> 409")
    void shouldRejectTerminatingTripBeforeDepartureTime() {
        LocalDateTime futureDeparture = LocalDateTime.now().plusHours(5);
        Trajet trajet = Trajet.builder()
                .id(100L)
                .conducteur(hamid)
                .vehicule(daciaLogan)
                .dateDepart(futureDeparture)
                .statut(StatutTrajet.PUBLIE)
                .build();

        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajet));

        assertThatThrownBy(() -> trajetService.terminerTrajet(100L, 1L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Un trajet ne peut être terminé qu'après son heure de départ");

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
    }

    @Test
    @DisplayName("Cas nominal : Hamid termine le trajet après l'heure de départ -> statut TERMINE")
    void shouldTerminateTripSuccessfullyAfterDepartureTime() {
        LocalDateTime pastDeparture = LocalDateTime.now().minusHours(2);
        Trajet trajet = Trajet.builder()
                .id(100L)
                .conducteur(hamid)
                .vehicule(daciaLogan)
                .dateDepart(pastDeparture)
                .statut(StatutTrajet.PUBLIE)
                .build();

        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajet));
        when(trajetRepository.save(any())).thenReturn(trajet);
        when(trajetMapper.toDetailResponse(any(), eq(0))).thenReturn(
                new TrajetDetailResponse(100L, "Rabat", "Marrakech", pastDeparture, 3, 0,
                        StatutTrajet.TERMINE, null, null, List.of())
        );

        TrajetDetailResponse response = trajetService.terminerTrajet(100L, 1L);

        assertThat(response.statut()).isEqualTo(StatutTrajet.TERMINE);
        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.TERMINE);
    }

    @Test
    @DisplayName("Scénario 11 : Hamid annule un trajet qui a deux réservations -> 204 ; trajet et réservations annulés ensemble")
    void shouldCancelTripAndAllConfirmedBookingsAtomically() {
        Trajet trajet = Trajet.builder()
                .id(100L)
                .conducteur(hamid)
                .vehicule(daciaLogan)
                .dateDepart(departureDate)
                .statut(StatutTrajet.PUBLIE)
                .build();

        Reservation res1 = Reservation.builder().id(201L).trajet(trajet).statut(StatutReservation.CONFIRMEE).build();
        Reservation res2 = Reservation.builder().id(202L).trajet(trajet).statut(StatutReservation.CONFIRMEE).build();

        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.findByTrajetId(100L)).thenReturn(List.of(res1, res2));

        trajetService.annulerTrajet(100L, 1L);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.ANNULE);
        assertThat(res1.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        assertThat(res2.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        verify(trajetRepository, times(1)).save(trajet);
        verify(reservationRepository, times(2)).save(any(Reservation.class));
    }
}
