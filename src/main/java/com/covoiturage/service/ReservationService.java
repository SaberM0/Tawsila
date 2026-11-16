package com.covoiturage.service;

import com.covoiturage.dto.request.ReservationCreateRequest;
import com.covoiturage.dto.response.ReservationResponse;

import java.util.List;

public interface ReservationService {

    ReservationResponse bookSeats(ReservationCreateRequest request, Long requestingPassengerId);

    void cancelReservation(Long reservationId, Long requestingPassengerId);

    ReservationResponse getReservationById(Long reservationId, Long requestingPassengerId);

    List<ReservationResponse> getReservationsByPassager(Long passagerId, Long requestingPassengerId);
}
