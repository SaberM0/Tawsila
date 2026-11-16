package com.covoiturage.service;

import com.covoiturage.domain.entity.Conducteur;
import com.covoiturage.domain.entity.PointArret;
import com.covoiturage.domain.entity.Trajet;
import com.covoiturage.domain.entity.Vehicule;
import com.covoiturage.domain.enums.StatutTrajet;
import com.covoiturage.domain.repository.TrajetRepository;
import com.covoiturage.dto.response.TrajetSearchResponse;
import com.covoiturage.dto.response.TrajetSearchResult;
import com.covoiturage.mapper.ConducteurMapper;
import com.covoiturage.mapper.PointArretMapper;
import com.covoiturage.mapper.VehiculeMapper;
import com.covoiturage.service.impl.TrajetServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrajetSearchTest {

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private ConducteurMapper conducteurMapper;

    @Mock
    private VehiculeMapper vehiculeMapper;

    @Mock
    private PointArretMapper pointArretMapper;

    @InjectMocks
    private TrajetServiceImpl trajetService;

    @Test
    @DisplayName("Scénario 2 : Karim recherche Settat -> Casablanca -> 200 ; le trajet de Hamid n'apparaît pas (sens inverse)")
    void scenario2_searchSettatToCasablanca_shouldReturnEmpty() {
        LocalDate searchDate = LocalDate.now().plusDays(3);
        Pageable pageable = PageRequest.of(0, 10);

        // La requête JPQL filtre p1.ordre < p2.ordre, donc pour Settat (ordre 2) vers Casablanca (ordre 1), 0 résultat retourné
        when(trajetRepository.searchTrajets(
                eq("Settat"),
                eq("Casablanca"),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(1),
                eq(null),
                eq(pageable)
        )).thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        Page<TrajetSearchResponse> result = trajetService.searchTrajets(
                "Settat", "Casablanca", searchDate, 1, null, pageable
        );

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Recherche valide : Casablanca -> Marrakech renvoie le trajet avec le prix de segment 80 DH")
    void searchCasablancaToMarrakech_shouldReturnMatchingTrip() {
        LocalDate searchDate = LocalDate.now().plusDays(3);
        Pageable pageable = PageRequest.of(0, 10);

        Trajet trajet = Trajet.builder()
                .id(100L)
                .villeDepart("Rabat")
                .villeArrivee("Marrakech")
                .dateDepart(searchDate.atTime(17, 0))
                .placesProposees(3)
                .statut(StatutTrajet.PUBLIE)
                .build();

        PointArret pCasa = PointArret.builder().id(11L).ville("Casablanca").ordre(1).prixDepuisDepart(new BigDecimal("40.00")).heurePassage(searchDate.atTime(18, 15)).build();
        PointArret pMarrakech = PointArret.builder().id(13L).ville("Marrakech").ordre(3).prixDepuisDepart(new BigDecimal("120.00")).heurePassage(searchDate.atTime(21, 30)).build();

        TrajetSearchResult searchResult = new TrajetSearchResult(
                trajet, pCasa, pMarrakech, new BigDecimal("80.00"), 0L
        );

        when(trajetRepository.searchTrajets(
                eq("Casablanca"),
                eq("Marrakech"),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(1),
                eq(null),
                eq(pageable)
        )).thenReturn(new PageImpl<>(List.of(searchResult), pageable, 1));

        Page<TrajetSearchResponse> result = trajetService.searchTrajets(
                "Casablanca", "Marrakech", searchDate, 1, null, pageable
        );

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        TrajetSearchResponse trip = result.getContent().get(0);
        assertThat(trip.villeDepart()).isEqualTo("Casablanca");
        assertThat(trip.villeArrivee()).isEqualTo("Marrakech");
        assertThat(trip.prixSegment()).isEqualByComparingTo("80.00");
        assertThat(trip.placesRestantes()).isEqualTo(3);
    }
}
