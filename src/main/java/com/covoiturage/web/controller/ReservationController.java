package com.covoiturage.web.controller;

import com.covoiturage.dto.request.ReservationCreateRequest;
import com.covoiturage.dto.response.ReservationResponse;
import com.covoiturage.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Réservations", description = "Gestion des réservations de places sur les segments de trajets")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Réserver des places sur un segment de trajet (passager)")
    public ResponseEntity<ReservationResponse> bookSeats(
            @RequestHeader(value = "X-Passager-Id", required = false) Long requestingPassengerId,
            @Valid @RequestBody ReservationCreateRequest request) {
        ReservationResponse response = reservationService.bookSeats(request, requestingPassengerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter les détails d'une réservation")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Passager-Id", required = false) Long requestingPassengerId) {
        return ResponseEntity.ok(reservationService.getReservationById(id, requestingPassengerId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler une réservation jusqu'à 24h avant le départ du trajet")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @RequestHeader(value = "X-Passager-Id", required = false) Long requestingPassengerId) {
        reservationService.cancelReservation(id, requestingPassengerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/passager/{passagerId}")
    @Operation(summary = "Consulter l'historique des réservations d'un passager")
    public ResponseEntity<List<ReservationResponse>> getReservationsByPassager(
            @PathVariable Long passagerId,
            @RequestHeader(value = "X-Passager-Id", required = false) Long requestingPassengerId) {
        return ResponseEntity.ok(reservationService.getReservationsByPassager(passagerId, requestingPassengerId));
    }
}
