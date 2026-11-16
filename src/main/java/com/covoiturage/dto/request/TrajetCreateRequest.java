package com.covoiturage.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TrajetCreateRequest(
        @NotNull(message = "L'identifiant du conducteur est obligatoire")
        Long conducteurId,

        @NotNull(message = "L'identifiant du véhicule est obligatoire")
        Long vehiculeId,

        @NotNull(message = "Le nombre de places proposées est obligatoire")
        @Min(value = 1, message = "Le nombre de places proposées doit être supérieur ou égal à 1")
        Integer placesProposees,

        @NotEmpty(message = "Le trajet doit comporter une liste de points d'arrêt")
        @Size(min = 2, message = "Un trajet doit comporter au minimum deux points d'arrêt (départ et arrivée)")
        List<@Valid PointArretRequest> pointsArret
) {}
