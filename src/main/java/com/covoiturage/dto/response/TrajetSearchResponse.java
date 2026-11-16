package com.covoiturage.dto.response;

import com.covoiturage.domain.enums.StatutTrajet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TrajetSearchResponse(
        Long trajetId,
        String villeDepart,
        String villeArrivee,
        LocalDateTime heureDepart,
        LocalDateTime heureArrivee,
        BigDecimal prixSegment,
        Integer placesRestantes,
        StatutTrajet statut,
        ConducteurResponse conducteur,
        VehiculeResponse vehicule,
        List<PointArretResponse> pointsArret
) {}
