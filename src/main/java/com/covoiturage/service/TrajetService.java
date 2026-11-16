package com.covoiturage.service;

import com.covoiturage.domain.entity.Trajet;
import com.covoiturage.dto.request.TrajetCreateRequest;
import com.covoiturage.dto.response.ReservationResponse;
import com.covoiturage.dto.response.TrajetDetailResponse;
import com.covoiturage.dto.response.TrajetSummaryResponse;

import java.util.List;

public interface TrajetService {

    TrajetDetailResponse publishTrajet(TrajetCreateRequest request, Long requestingDriverId);

    TrajetDetailResponse getTrajetById(Long id);

    List<TrajetSummaryResponse> getTrajetsByConducteur(Long conducteurId);

    int calculatePlacesRestantes(Trajet trajet);

    void checkAndUpdateTrajetStatus(Trajet trajet);

    List<ReservationResponse> getReservationsByTrajet(Long trajetId, Long requestingDriverId);

    TrajetDetailResponse terminerTrajet(Long trajetId, Long requestingDriverId);

    void annulerTrajet(Long trajetId, Long requestingDriverId);

    org.springframework.data.domain.Page<com.covoiturage.dto.response.TrajetSearchResponse> searchTrajets(
            String villeDepart,
            String villeArrivee,
            java.time.LocalDate dateDepart,
            Integer placesDemandees,
            java.math.BigDecimal maxPrix,
            org.springframework.data.domain.Pageable pageable);
}
