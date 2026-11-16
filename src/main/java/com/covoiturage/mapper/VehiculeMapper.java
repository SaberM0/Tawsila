package com.covoiturage.mapper;

import com.covoiturage.domain.entity.Vehicule;
import com.covoiturage.dto.request.VehiculeCreateRequest;
import com.covoiturage.dto.request.VehiculeUpdateRequest;
import com.covoiturage.dto.response.VehiculeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    @Mapping(target = "trajets", ignore = true)
    Vehicule toEntity(VehiculeCreateRequest request);

    @Mapping(target = "conducteurId", source = "conducteur.id")
    @Mapping(target = "conducteurNomComplet", expression = "java(entity.getConducteur() != null ? entity.getConducteur().getPrenom() + \" \" + entity.getConducteur().getNom() : null)")
    VehiculeResponse toResponse(Vehicule entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    @Mapping(target = "trajets", ignore = true)
    void updateEntityFromRequest(VehiculeUpdateRequest request, @MappingTarget Vehicule entity);
}
