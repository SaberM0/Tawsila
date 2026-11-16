package com.covoiturage.web.controller;

import com.covoiturage.dto.request.ConducteurCreateRequest;
import com.covoiturage.dto.request.ConducteurUpdateRequest;
import com.covoiturage.dto.response.ConducteurResponse;
import com.covoiturage.service.ConducteurService;
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
@RequestMapping("/api/v1/conducteurs")
@RequiredArgsConstructor
@Tag(name = "Conducteurs", description = "Gestion des profils et comptes conducteurs")
public class ConducteurController {

    private final ConducteurService conducteurService;

    @PostMapping
    @Operation(summary = "Créer un nouveau profil conducteur")
    public ResponseEntity<ConducteurResponse> createConducteur(@Valid @RequestBody ConducteurCreateRequest request) {
        ConducteurResponse response = conducteurService.createConducteur(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter les informations d'un conducteur par son ID")
    public ResponseEntity<ConducteurResponse> getConducteurById(@PathVariable Long id) {
        return ResponseEntity.ok(conducteurService.getConducteurById(id));
    }

    @GetMapping
    @Operation(summary = "Consulter la liste paginée des conducteurs")
    public ResponseEntity<Page<ConducteurResponse>> getAllConducteurs(
            @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(conducteurService.getAllConducteurs(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier le profil d'un conducteur")
    public ResponseEntity<ConducteurResponse> updateConducteur(
            @PathVariable Long id,
            @Valid @RequestBody ConducteurUpdateRequest request) {
        return ResponseEntity.ok(conducteurService.updateConducteur(id, request));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des conducteurs par nom, prénom ou email")
    public ResponseEntity<Page<ConducteurResponse>> searchConducteurs(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(conducteurService.searchConducteurs(keyword, pageable));
    }
}
