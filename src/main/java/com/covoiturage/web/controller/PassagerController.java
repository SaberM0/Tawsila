package com.covoiturage.web.controller;

import com.covoiturage.dto.request.PassagerCreateRequest;
import com.covoiturage.dto.request.PassagerUpdateRequest;
import com.covoiturage.dto.response.PassagerResponse;
import com.covoiturage.service.PassagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/passagers")
@RequiredArgsConstructor
@Tag(name = "Passagers", description = "Gestion des profils et comptes passagers")
public class PassagerController {

    private final PassagerService passagerService;

    @PostMapping
    @Operation(summary = "Créer un nouveau compte passager")
    public ResponseEntity<PassagerResponse> createPassager(@Valid @RequestBody PassagerCreateRequest request) {
        PassagerResponse response = passagerService.createPassager(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le profil d'un passager par son ID")
    public ResponseEntity<PassagerResponse> getPassagerById(@PathVariable Long id) {
        return ResponseEntity.ok(passagerService.getPassagerById(id));
    }

    @GetMapping
    @Operation(summary = "Consulter la liste paginée des passagers")
    public ResponseEntity<Page<PassagerResponse>> getAllPassagers(
            @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(passagerService.getAllPassagers(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier les informations d'un passager")
    public ResponseEntity<PassagerResponse> updatePassager(
            @PathVariable Long id,
            @Valid @RequestBody PassagerUpdateRequest request) {
        return ResponseEntity.ok(passagerService.updatePassager(id, request));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des passagers par email")
    public ResponseEntity<Page<PassagerResponse>> searchPassagers(
            @RequestParam(required = false) String email,
            @PageableDefault(size = 10, sort = "email", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(passagerService.searchPassagersByEmail(email, pageable));
    }
}
