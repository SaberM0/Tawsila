package com.covoiturage.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservationCreateRequest(
        @NotNull(message = "L'identifiant du passager est obligatoire")
        Long passagerId,

        @NotNull(message = "L'identifiant du trajet est obligatoire")
        Long trajetId,

        @NotNull(message = "L'identifiant du point de montée est obligatoire")
        Long pointMonteeId,

        @NotNull(message = "L'identifiant du point de descente est obligatoire")
        Long pointDescenteId,

        @NotNull(message = "Le nombre de places est obligatoire")
        @Min(value = 1, message = "Vous devez réserver au minimum 1 place")
        Integer nombrePlaces
) {}
