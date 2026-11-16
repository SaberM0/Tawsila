package com.covoiturage.dto.response;

import com.covoiturage.domain.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long passagerId,
        String passagerNomComplet,
        Long trajetId,
        String villeMontee,
        String villeDescente,
        Integer nombrePlaces,
        BigDecimal montantTotal,
        LocalDateTime dateReservation,
        StatutReservation statut,
        Integer placesRestantes
) {}
