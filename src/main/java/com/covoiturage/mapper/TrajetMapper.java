package com.covoiturage.mapper;

import com.covoiturage.domain.entity.Trajet;
import com.covoiturage.dto.response.TrajetDetailResponse;
import com.covoiturage.dto.response.TrajetSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ConducteurMapper.class, VehiculeMapper.class, PointArretMapper.class})
public interface TrajetMapper {

    @Mapping(target = "placesRestantes", source = "placesRestantes")
    TrajetDetailResponse toDetailResponse(Trajet entity, Integer placesRestantes);

    @Mapping(target = "placesRestantes", source = "placesRestantes")
    @Mapping(target = "conducteurNomComplet", expression = "java(entity.getConducteur() != null ? entity.getConducteur().getPrenom() + \" \" + entity.getConducteur().getNom() : null)")
    @Mapping(target = "vehiculeImmatriculation", expression = "java(entity.getVehicule() != null ? entity.getVehicule().getImmatriculation() : null)")
    TrajetSummaryResponse toSummaryResponse(Trajet entity, Integer placesRestantes);
}
