package com.covoiturage.dto.response;

import com.covoiturage.domain.enums.StatutTrajet;

import java.time.LocalDateTime;

public record TrajetSummaryResponse(
        Long id,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateDepart,
        Integer placesProposees,
        Integer placesRestantes,
        StatutTrajet statut,
        String conducteurNomComplet,
        String vehiculeImmatriculation
) {}
