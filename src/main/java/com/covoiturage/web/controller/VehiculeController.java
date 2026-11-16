package com.covoiturage.web.controller;

import com.covoiturage.dto.request.VehiculeCreateRequest;
import com.covoiturage.dto.request.VehiculeUpdateRequest;
import com.covoiturage.dto.response.VehiculeResponse;
import com.covoiturage.service.VehiculeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicules")
@RequiredArgsConstructor
@Tag(name = "Véhicules", description = "Gestion du parc de véhicules des conducteurs")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @PostMapping
    @Operation(summary = "Enregistrer un nouveau véhicule pour un conducteur")
    public ResponseEntity<VehiculeResponse> addVehicule(@Valid @RequestBody VehiculeCreateRequest request) {
        VehiculeResponse response = vehiculeService.addVehicule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter les détails d'un véhicule")
    public ResponseEntity<VehiculeResponse> getVehiculeById(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.getVehiculeById(id));
    }

    @GetMapping("/conducteur/{conducteurId}")
    @Operation(summary = "Lister tous les véhicules d'un conducteur")
    public ResponseEntity<List<VehiculeResponse>> getVehiculesByConducteur(@PathVariable Long conducteurId) {
        return ResponseEntity.ok(vehiculeService.getVehiculesByConducteur(conducteurId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier les informations d'un véhicule")
    public ResponseEntity<VehiculeResponse> updateVehicule(
            @PathVariable Long id,
            @RequestHeader(value = "X-Conducteur-Id", required = false) Long requestingDriverId,
            @Valid @RequestBody VehiculeUpdateRequest request) {
        return ResponseEntity.ok(vehiculeService.updateVehicule(id, requestingDriverId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un véhicule (lorsqu'il n'est associé à aucun trajet)")
    public ResponseEntity<Void> deleteVehicule(
            @PathVariable Long id,
            @RequestHeader(value = "X-Conducteur-Id", required = false) Long requestingDriverId) {
        vehiculeService.deleteVehicule(id, requestingDriverId);
        return ResponseEntity.noContent().build();
    }
}
