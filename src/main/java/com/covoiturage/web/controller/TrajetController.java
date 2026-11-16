package com.covoiturage.web.controller;

import com.covoiturage.dto.request.TrajetCreateRequest;
import com.covoiturage.dto.response.ReservationResponse;
import com.covoiturage.dto.response.TrajetDetailResponse;
import com.covoiturage.dto.response.TrajetSummaryResponse;
import com.covoiturage.service.TrajetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trajets")
@RequiredArgsConstructor
@Tag(name = "Trajets", description = "Publication et consultation des trajets multi-étapes")
public class TrajetController {

    private final TrajetService trajetService;

    @PostMapping
    @Operation(summary = "Publier un nouveau trajet multi-étapes avec points d'arrêt ordonnés")
    public ResponseEntity<TrajetDetailResponse> publishTrajet(
            @RequestHeader(value = "X-Conducteur-Id", required = false) Long requestingDriverId,
            @Valid @RequestBody TrajetCreateRequest request) {
        TrajetDetailResponse response = trajetService.publishTrajet(request, requestingDriverId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter les détails d'un trajet avec son parcours et places restantes")
    public ResponseEntity<TrajetDetailResponse> getTrajetById(@PathVariable Long id) {
        return ResponseEntity.ok(trajetService.getTrajetById(id));
    }

    @GetMapping("/conducteur/{conducteurId}")
    @Operation(summary = "Consulter tous les trajets publiés par un conducteur")
    public ResponseEntity<List<TrajetSummaryResponse>> getTrajetsByConducteur(@PathVariable Long conducteurId) {
        return ResponseEntity.ok(trajetService.getTrajetsByConducteur(conducteurId));
    }

    @GetMapping("/{id}/reservations")
    @Operation(summary = "Consulter les réservations associées à un trajet (conducteur propriétaire)")
    public ResponseEntity<List<ReservationResponse>> getReservationsByTrajet(
            @PathVariable Long id,
            @RequestHeader(value = "X-Conducteur-Id", required = false) Long requestingDriverId) {
        return ResponseEntity.ok(trajetService.getReservationsByTrajet(id, requestingDriverId));
    }
}
