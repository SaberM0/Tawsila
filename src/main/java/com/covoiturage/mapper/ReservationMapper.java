package com.covoiturage.mapper;

import com.covoiturage.domain.entity.Reservation;
import com.covoiturage.dto.response.ReservationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "passagerId", source = "passager.id")
    @Mapping(target = "passagerNomComplet", expression = "java(entity.getPassager() != null ? entity.getPassager().getPrenom() + \" \" + entity.getPassager().getNom() : null)")
    @Mapping(target = "trajetId", source = "trajet.id")
    @Mapping(target = "villeMontee", source = "pointMontee.ville")
    @Mapping(target = "villeDescente", source = "pointDescente.ville")
    ReservationResponse toResponse(Reservation entity);

    List<ReservationResponse> toResponseList(List<Reservation> entities);
}
