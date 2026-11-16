package com.covoiturage.dto.response;

import com.covoiturage.domain.entity.PointArret;
import com.covoiturage.domain.entity.Trajet;

import java.math.BigDecimal;

public record TrajetSearchResult(
        Trajet trajet,
        PointArret pointDepart,
        PointArret pointArrivee,
        BigDecimal prixSegment,
        Long reservedSeats
) {}
