package com.covoiturage.service;

import com.covoiturage.domain.entity.*;
import com.covoiturage.domain.enums.StatutReservation;
import com.covoiturage.domain.enums.StatutTrajet;
import com.covoiturage.domain.repository.PassagerRepository;
import com.covoiturage.domain.repository.PointArretRepository;
import com.covoiturage.domain.repository.ReservationRepository;
import com.covoiturage.domain.repository.TrajetRepository;
import com.covoiturage.dto.request.ReservationCreateRequest;
import com.covoiturage.dto.response.ReservationResponse;
import com.covoiturage.exception.BusinessConflictException;
import com.covoiturage.exception.BusinessValidationException;
import com.covoiturage.mapper.ReservationMapper;
import com.covoiturage.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private PassagerRepository passagerRepository;

    @Mock
    private PointArretRepository pointArretRepository;

    @Mock
    private TrajetService trajetService;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Passager salma;
    private Passager karim;
    private Passager imane;
    private Trajet trajetHamid;
    private PointArret pRabat;
    private PointArret pCasa;
    private PointArret pSettat;
    private PointArret pMarrakech;
    private LocalDateTime departureDate;

    @BeforeEach
    void setUp() {
        departureDate = LocalDateTime.now().plusDays(3).withHour(17).withMinute(0);

        salma = Passager.builder().id(2L).nom("Chraibi").prenom("Salma").email("salma@covoiturage.ma").build();
        karim = Passager.builder().id(1L).nom("Alami").prenom("Karim").email("karim@covoiturage.ma").build();
        imane = Passager.builder().id(3L).nom("Mansouri").prenom("Imane").email("imane@covoiturage.ma").build();

        trajetHamid = Trajet.builder()
                .id(100L)
                .villeDepart("Rabat")
                .villeArrivee("Marrakech")
                .dateDepart(departureDate)
                .placesProposees(3)
                .statut(StatutTrajet.PUBLIE)
                .build();

        pRabat = PointArret.builder().id(10L).trajet(trajetHamid).ville("Rabat").ordre(0).prixDepuisDepart(new BigDecimal("0.00")).build();
        pCasa = PointArret.builder().id(11L).trajet(trajetHamid).ville("Casablanca").ordre(1).prixDepuisDepart(new BigDecimal("40.00")).build();
        pSettat = PointArret.builder().id(12L).trajet(trajetHamid).ville("Settat").ordre(2).prixDepuisDepart(new BigDecimal("70.00")).build();
        pMarrakech = PointArret.builder().id(13L).trajet(trajetHamid).ville("Marrakech").ordre(3).prixDepuisDepart(new BigDecimal("120.00")).build();
    }

    @Test
    @DisplayName("Scénario 1 : Salma réserve 1 place Settat -> Marrakech -> 201 ; montant 50 DH ; 2 places restantes")
    void scenario1_salmaReservesOneSeatSettatToMarrakech() {
        ReservationCreateRequest request = new ReservationCreateRequest(2L, 100L, 12L, 13L, 1);

        when(passagerRepository.findById(2L)).thenReturn(Optional.of(salma));
        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajetHamid));
        when(pointArretRepository.findById(12L)).thenReturn(Optional.of(pSettat));
        when(pointArretRepository.findById(13L)).thenReturn(Optional.of(pMarrakech));
        when(reservationRepository.existsByTrajetIdAndPassagerIdAndStatut(100L, 2L, StatutReservation.CONFIRMEE)).thenReturn(false);
        when(trajetService.calculatePlacesRestantes(trajetHamid)).thenReturn(3);

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(501L);
            return r;
        });

        when(reservationMapper.toResponse(any(Reservation.class), eq(2))).thenReturn(
                new ReservationResponse(501L, 2L, "Salma Chraibi", 100L, "Settat", "Marrakech",
                        1, new BigDecimal("50.00"), LocalDateTime.now(), StatutReservation.CONFIRMEE, 2)
        );

        ReservationResponse response = reservationService.bookSeats(request, 2L);

        assertThat(response).isNotNull();
        assertThat(response.montantTotal()).isEqualByComparingTo("50.00");
        assertThat(response.placesRestantes()).isEqualTo(2);
        assertThat(trajetHamid.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
    }

    @Test
    @DisplayName("Scénario 3 : Karim réserve 2 places Casablanca -> Marrakech -> 201 ; montant 160 DH ; trajet COMPLET")
    void scenario3_karimReservesTwoSeatsCasablancaToMarrakech_trajetBecomesComplet() {
        ReservationCreateRequest request = new ReservationCreateRequest(1L, 100L, 11L, 13L, 2);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(karim));
        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajetHamid));
        when(pointArretRepository.findById(11L)).thenReturn(Optional.of(pCasa));
        when(pointArretRepository.findById(13L)).thenReturn(Optional.of(pMarrakech));
        when(reservationRepository.existsByTrajetIdAndPassagerIdAndStatut(100L, 1L, StatutReservation.CONFIRMEE)).thenReturn(false);
        when(trajetService.calculatePlacesRestantes(trajetHamid)).thenReturn(2);

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(502L);
            return r;
        });

        when(reservationMapper.toResponse(any(Reservation.class), eq(0))).thenReturn(
                new ReservationResponse(502L, 1L, "Karim Alami", 100L, "Casablanca", "Marrakech",
                        2, new BigDecimal("160.00"), LocalDateTime.now(), StatutReservation.CONFIRMEE, 0)
        );

        ReservationResponse response = reservationService.bookSeats(request, 1L);

        assertThat(response.montantTotal()).isEqualByComparingTo("160.00");
        assertThat(response.placesRestantes()).isEqualTo(0);
        assertThat(trajetHamid.getStatut()).isEqualTo(StatutTrajet.COMPLET);
        verify(trajetRepository, times(1)).save(trajetHamid);
    }

    @Test
    @DisplayName("Scénario 4 : Imane réserve 1 place sur un trajet COMPLET -> 409")
    void scenario4_imaneReservesOnCompletTrip_throws409() {
        trajetHamid.setStatut(StatutTrajet.COMPLET);
        ReservationCreateRequest request = new ReservationCreateRequest(3L, 100L, 10L, 13L, 1);

        when(passagerRepository.findById(3L)).thenReturn(Optional.of(imane));
        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajetHamid));

        assertThatThrownBy(() -> reservationService.bookSeats(request, 3L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Le trajet est complet");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Scénario 5 : Karim annule sa réservation 3 jours avant -> 204 ; trajet de nouveau PUBLIE")
    void scenario5_karimCancelsThreeDaysBefore_trajetBecomesPublieAgain() {
        trajetHamid.setStatut(StatutTrajet.COMPLET);
        Reservation reservation = Reservation.builder()
                .id(502L)
                .passager(karim)
                .trajet(trajetHamid)
                .nombrePlaces(2)
                .statut(StatutReservation.CONFIRMEE)
                .build();

        when(reservationRepository.findById(502L)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(502L, 1L);

        assertThat(reservation.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        assertThat(trajetHamid.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
        verify(reservationRepository, times(1)).save(reservation);
        verify(trajetRepository, times(1)).save(trajetHamid);
    }

    @Test
    @DisplayName("Scénario 6 : Imane réserve avec la montée à Marrakech et la descente à Settat -> 400 ; montée après la descente")
    void scenario6_imaneReservesWithBoardingAfterAlighting_throws400() {
        ReservationCreateRequest request = new ReservationCreateRequest(3L, 100L, 13L, 12L, 1);

        when(passagerRepository.findById(3L)).thenReturn(Optional.of(imane));
        when(trajetRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(trajetHamid));
        when(pointArretRepository.findById(13L)).thenReturn(Optional.of(pMarrakech));
        when(pointArretRepository.findById(12L)).thenReturn(Optional.of(pSettat));

        assertThatThrownBy(() -> reservationService.bookSeats(request, 3L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("montée après la descente");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Scénario 10 : Salma annule 2 heures avant le départ -> 409 ; délai de 24 h dépassé")
    void scenario10_salmaCancelsTwoHoursBeforeDeparture_throws409() {
        trajetHamid.setDateDepart(LocalDateTime.now().plusHours(2));
        Reservation reservation = Reservation.builder()
                .id(501L)
                .passager(salma)
                .trajet(trajetHamid)
                .statut(StatutReservation.CONFIRMEE)
                .build();

        when(reservationRepository.findById(501L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(501L, 2L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("délai de 24 h dépassé");

        assertThat(reservation.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);
    }
}
