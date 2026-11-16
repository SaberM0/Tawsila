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

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des trajets entre deux étapes avec calcul automatique du prix de segment et places restantes")
    public ResponseEntity<org.springframework.data.domain.Page<com.covoiturage.dto.response.TrajetSearchResponse>> searchTrajets(
            @RequestParam String villeDepart,
            @RequestParam String villeArrivee,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dateDepart,
            @RequestParam(required = false, defaultValue = "1") Integer places,
            @RequestParam(required = false) java.math.BigDecimal maxPrix,
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(trajetService.searchTrajets(villeDepart, villeArrivee, dateDepart, places, maxPrix, pageable));
    }

    @PutMapping("/{id}/terminer")
    @Operation(summary = "Marquer un trajet comme terminé (après son heure de départ)")
    public ResponseEntity<TrajetDetailResponse> terminerTrajet(
            @PathVariable Long id,
            @RequestHeader(value = "X-Conducteur-Id", required = false) Long requestingDriverId) {
        return ResponseEntity.ok(trajetService.terminerTrajet(id, requestingDriverId));
    }

    @PutMapping("/{id}/annuler")
    @Operation(summary = "Annuler un trajet et toutes ses réservations associées (conducteur propriétaire)")
    public ResponseEntity<Void> annulerTrajet(
            @PathVariable Long id,
            @RequestHeader(value = "X-Conducteur-Id", required = false) Long requestingDriverId) {
        trajetService.annulerTrajet(id, requestingDriverId);
        return ResponseEntity.noContent().build();
    }
}
