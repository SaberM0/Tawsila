package com.covoiturage.mapper;

import com.covoiturage.domain.entity.Reservation;
import com.covoiturage.dto.response.ReservationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "passagerId", source = "entity.passager.id")
    @Mapping(target = "passagerNomComplet", expression = "java(entity.getPassager() != null ? entity.getPassager().getPrenom() + \" \" + entity.getPassager().getNom() : null)")
    @Mapping(target = "trajetId", source = "entity.trajet.id")
    @Mapping(target = "villeMontee", source = "entity.pointMontee.ville")
    @Mapping(target = "villeDescente", source = "entity.pointDescente.ville")
    @Mapping(target = "placesRestantes", source = "placesRestantes")
    ReservationResponse toResponse(Reservation entity, Integer placesRestantes);

    default ReservationResponse toResponse(Reservation entity) {
        return toResponse(entity, null);
    }

    default List<ReservationResponse> toResponseList(List<Reservation> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).toList();
    }
}
